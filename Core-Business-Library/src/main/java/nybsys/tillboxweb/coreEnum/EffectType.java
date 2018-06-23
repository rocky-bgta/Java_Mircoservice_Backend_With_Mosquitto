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

public enum EffectType
{
    Increase(1),
    Decrease(2);

    private int effectType;

    EffectType(int effectType) {
        this.effectType = effectType;
    }

    public int get() {
        return this.effectType;
    }

    private static final Map<String, Integer> MAP = new TreeMap<>();

    static {
        for (EffectType kv : EffectType.values()) {
            MAP.put(kv.name(), kv.get());
        }
    }

    public static Map<String, Integer> getMAP() {
        return MAP;
    }

}