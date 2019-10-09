package com.westsword.stocks.base;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


public class ConvertDD2Double{
    byte unk_DB6A14[] = {
        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
    };
    
    boolean sub_489F40(byte[] dd, int a2)
    {
        int v2; // ecx@1
        byte[] v3; // esi@3
        boolean v4; // zf@3
        byte[] v5; // edi@3
    
        v2 = a2;
        if ( a2 > 0x10 )
            v2 = 16;
        v3 = dd;
        v5 = unk_DB6A14;
        v4 = true;
        int idx=0;
        do
        {
            if ( v2 == 0  )
                break;
            byte b1=v3[idx];
            byte b2=v5[idx];
            v4 = b1 == b2;
            idx++;
            v2--;
        } while ( v4 );
    
        return v4;
    }
    
    boolean sub_48A090(byte[] dd)
    {
        boolean v1; // eax@2
    
        ByteBuffer buf = ByteBuffer.wrap(dd);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        int i = buf.getInt();
        if ( i == -2147483648 )
            v1 = true;
        else {
            v1 = sub_489F40(dd, 4);
        }
    
        return v1;
    }
    
    byte dword_DB6A24[]={
        (byte)0x1,  (byte)0,   (byte)0,   (byte)0, 
        (byte)0xa,  (byte)0,   (byte)0,   (byte)0, 
        (byte)0x64, (byte)0,   (byte)0,   (byte)0, 
        (byte)0xe8, (byte)0x3, (byte)0,   (byte)0, 
        (byte)0x10, (byte)0x27,(byte)0,   (byte)0, 
        (byte)0xa0, (byte)0x86,(byte)0x1, (byte)0, 
        (byte)0x40, (byte)0x42,(byte)0xf, (byte)0, 
        (byte)0x80, (byte)0x96,(byte)0x98,(byte)0,
    };
    
    byte dbl_E06A08[] = {
        (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0x80,
    };
    
    public double sub_48A0D0(byte[] dd)
    {
        byte[] v1; // esi@1
        double result; // st7@2
        int v3; // eax@3
        int v4; // ecx@3
        double v5; // st7@5
        ByteBuffer buf;
    
        v1 = dd;
        if ( sub_48A090(v1) )
        {
            buf = ByteBuffer.wrap(dbl_E06A08);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            result = buf.getDouble();
        }
        else
        {
            buf = ByteBuffer.wrap(v1);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            v3 = buf.getInt();
            //System.out.format("dd=%x\n", v3);
            v4 = v3 & 0x7FFFFFF;
            if ( v4 != 0 )
            {
                int offset =  (v3>>28) & 7;
                offset=offset * 4;
                buf = ByteBuffer.wrap(dword_DB6A24, offset, 4);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                int i=buf.getInt();
                ByteBuffer buf2=ByteBuffer.allocate(8);
                buf2.putInt(i);
                buf2.putInt(0);
                v5 = buf2.getDouble(0);
                //System.out.format("offset=%d v4=%x v5=%f\n", offset, v4, v5);
    
                ByteBuffer buf4 = ByteBuffer.allocate(8);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                buf4.putInt(v4);
                buf4.putInt(0);
                double d4=buf4.getDouble(0);
    
                if ( (v3 & 0x80000000) == -2147483648 ) {
                    result = d4 / v5;
                } else {
                    result = v5 * d4;
                }
    
                if ( (v3 & 0x8000000) != 0 )
                    result = -result;
            }
            else
            {
                result = 0.0;
            }
        }
    
        return result;
    }

    public double sub_48A0D0(String dd) {
        double d;

        Long lDD = Long.parseLong(dd, 16);
        int iDD = lDD.intValue();
        d = sub_48A0D0(iDD);
        //System.out.format("iDD=%x\n", iDD);

        return d;
    }

    public double sub_48A0D0(int dd) {
        double result;

        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf = buf.putInt(dd);
        byte[] bDD=buf.array();

        result = sub_48A0D0(bDD);

        return result;
    }
}
