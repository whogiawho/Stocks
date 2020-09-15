 /*
 Copyright (C) 2019-2050 WestSword, Inc.
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.  */
 
 /* Written by whogiawho <whogiawho@gmail.com>. */
 
 
package com.westsword.stocks.analyze;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.utils.*;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class RealtimeAnalyze {
    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private boolean trace = false;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }


    /**
     * Creates a WatchService and registers the given directory
     */
    RealtimeAnalyze(Path dir0, Path dir1, String outAnalysisFile) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();

        mDetailsDir=dir0;
        mRTPankouDir=dir1;
        mOutAnalysisFile = outAnalysisFile;

        register(dir0);
        register(dir1);

        // enable trace after initial registration
        this.trace = Settings.getSwitch(Settings.SWITCH_OF_MAIN_LOOP);
    }

    public String getAnalysisFile() {
        return mOutAnalysisFile;
    }

    private Path mDetailsDir=null;
    private Path mRTPankouDir=null;
    private String mOutAnalysisFile="";

    //RawTradeDetails
    TreeSet<String> mRawDetailsSet = new TreeSet<String>();
    private boolean isRawDetailFileProcessed(String inFile) {
        return mRawDetailsSet.contains(inFile);
    }
    private void processRawDetails(String inFile, ArrayList<RawTradeDetails> rawDetailsList) {
        if(inFile == null) {
            return;
        }

        RawTradeDetailsList rtdList = new RawTradeDetailsList();
        rtdList.load(rawDetailsList, inFile);
    }

    //RawPankou
    TreeSet<String> mRawPankouSet = new TreeSet<String>();
    private boolean isRawPankouProcessed(String inFile) {
        return mRawPankouSet.contains(inFile);
    }
    private void processRawPankou(String inFile, ArrayList<RawRTPankou> rawPankouList) {
        if(inFile == null) {
            return;
        }

        RawRTPankouList rrpList = new RawRTPankouList();
        rrpList.load(rawPankouList, inFile);
    }

    private boolean contains(ArrayList<WatchEvent<?>> list, String kindName, Path p, Path dir) {
        boolean bContain = false;

        for(int i=0; i<list.size(); i++) {
            WatchEvent<?> event = list.get(i);
            WatchEvent<Path> ev = cast(event);
            Path evName = ev.context();
            Path evChild = dir.resolve(evName);


            String kindName0 = event.kind().name();
            if(kindName0.equals(kindName) && evChild.getFileName().equals(p.getFileName())) {
                bContain = true;
                break;
            }
        }

        return bContain;
    }

    //retrieve a unique list
    private List<WatchEvent<?>> uniqEvents(Path dir, List<WatchEvent<?>> list) {
        ArrayList<WatchEvent<?>> l = new ArrayList<WatchEvent<?>>();

        for (WatchEvent<?> event: list) {
            WatchEvent<Path> ev = cast(event);
            Path name = ev.context();
            if(name==null) {
                System.err.println("name=null");
                System.err.println(dir.getFileName());
                continue;
            }
            Path child = dir.resolve(name);
            WatchEvent.Kind kind = event.kind();
            if(contains(l, kind.name(), child, dir))
                continue;
            l.add(event);
        }

        return l;
    }


    private void processNewData(Analyze600030 way, 
            ArrayList<RawTradeDetails> rawDetailsList, ArrayList<RawRTPankou> rawPankouList) {

        boolean bFinished = false;
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            Path dir = keys.get(key);
            if (dir == null && trace) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            List<WatchEvent<?>> list = key.pollEvents();
            if(trace)
                System.out.format("original size = %d\n", list.size());
            List<WatchEvent<?>> uList = uniqEvents(dir, list);
            if(trace)
                System.out.format("modified size = %d\n", uList.size());

            for (WatchEvent<?> event: uList) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                String sChildAbsName = child.toString();

                // print out event
                if(trace)
                    System.out.format("%s: %s\n", event.kind().name(), child);

                if(kind.name().equals("ENTRY_MODIFY")) {
                    // read file to memory
                    if(skip2Process(sChildAbsName)) {      //process sChildAbsName only once
                        fileProcessed(sChildAbsName);
                        continue;
                    } else {
                        processWatchedFile(sChildAbsName, rawDetailsList, rawPankouList);
                    }

                    way.startAnalyze(rawDetailsList, rawPankouList);
                    if(way.isLastRawTradeDetailHandled()||way.isLastPankouHandled()) {
                        bFinished = true;
                        break;
                    }
                }
            }

            if(bFinished)
                break;
            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
    private void fileProcessed(String sFile) {
        if(trace) {
            String line = String.format("%s: %s was already processed!", 
                    Utils.getCallerName(getClass()), sFile);
            line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
            System.err.format("%s\n", line);
        }
    }
    private void processWatchedFile(String sChildAbsName, 
            ArrayList<RawTradeDetails> rawDetailsList, ArrayList<RawRTPankou> rawPankouList) {
        if(sChildAbsName.contains(mDetailsDir.toString())) {
            mRawDetailsSet.add(sChildAbsName);
            // to rawDetailsList
            processRawDetails(sChildAbsName, rawDetailsList);
        } else if(sChildAbsName.contains(mRTPankouDir.toString())) {
            mRawPankouSet.add(sChildAbsName);
            // to rawPankouList
            processRawPankou(sChildAbsName, rawPankouList); 
        } else {
            System.err.println("unsupported path:" + sChildAbsName);
        }
    }
    private boolean skip2Process(String sChildAbsName) {
        boolean bSkip = false;
        if(sChildAbsName.contains(mDetailsDir.toString())) {
            if(isRawDetailFileProcessed(sChildAbsName))
                bSkip = true;
        } else if(sChildAbsName.contains(mRTPankouDir.toString())) {
            if(isRawPankouProcessed(sChildAbsName))
                bSkip = true;
        }

        return bSkip;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    private void processEvents() {
        //the array to contain raw traded data <time, parsed price, count, type>
        ArrayList<RawTradeDetails> rawDetailsList = new ArrayList<RawTradeDetails>();
        RawTradeDetailsList rtdList = new RawTradeDetailsList();
        rtdList.loadPrevRawDetails(rawDetailsList, mDetailsDir.toString());
        System.out.format("%s: rawDetailsList.size()=%d\n", 
                Utils.getCallerName(getClass()), rawDetailsList.size());

        //the array to contain raw pankou data
        ArrayList<RawRTPankou> rawPankouList = new ArrayList<RawRTPankou>();
        RawRTPankouList rrpList = new RawRTPankouList();
        rrpList.loadPrevPankou(rawPankouList, mRTPankouDir.toString());
        System.out.format("%s: rawPankouList.size()=%d\n", 
                Utils.getCallerName(getClass()), rawPankouList.size());

        Analyze600030 way = new Analyze600030(this);
        //process previous rawTradeDetails&rawRTPankou already existing
        way.startAnalyze(rawDetailsList, rawPankouList);
        System.out.format("%s: way.startAnalyze() completes 1st run!\n", Utils.getCallerName(getClass()));

        processNewData(way, rawDetailsList, rawPankouList);
        System.out.format("%s: processNewData completed!\n", Utils.getCallerName(getClass()));

        // process last record
        processRawDetails(null, rawDetailsList);
        processRawPankou(null, rawPankouList);
    }

    private static void usage() {
        System.err.println("usage: java RealtimeAnalyze detailsDir rtpankouDir outAnalysisFile");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        // parse arguments
        if (args.length == 0 || args.length > 3)
            usage();

        // register directory and process its events
        Path dir0 = Paths.get(args[0]);                                               //details dir
        Path dir1 = Paths.get(args[1]);                                               //pankou dir
        String outAnalysisFile = args[2];                                             //output of the analysis file
        new RealtimeAnalyze(dir0, dir1, outAnalysisFile).processEvents();
    }
}
