/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 15/05/2018
 * Time: 02:06
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.coreEnum;

import java.util.Map;
import java.util.TreeMap;

public enum RoundingType
{
    RoundUp(1),
    RoundDown(2);

    private int roundingType;

    RoundingType(int roundingType) {
        this.roundingType = roundingType;
    }

    public int get() {
        return this.roundingType;
    }

    private static final Map<String, Integer> MAP = new TreeMap<>();

    static {
        for (RoundingType kv : RoundingType.values()) {
            MAP.put(kv.name(), kv.get());
        }
    }

    public static Map<String, Integer> getMAP() {
        return MAP;
    }

}