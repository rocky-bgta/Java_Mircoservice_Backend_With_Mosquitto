/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 17/05/2018
 * Time: 06:37
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.companySettingUtils;

import nybsys.tillboxweb.coreEnum.RoundingType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

public class RoundingUtils {
    private static final Logger log = LoggerFactory.getLogger(RoundingUtils.class);

    public static Double getRoundedAmount(Integer numberOfDigitAfterDecimalPoint, Integer roundNearestTo, Integer roundingType, Double value) {
        Double calculatedAmount = 0.0;
        String pattern = "#";
        String numberOfDigitAfterDecimalPointTxt ="1";
        if (numberOfDigitAfterDecimalPoint > 0) {
            pattern += ".";
            pattern += StringUtils.repeat("0", numberOfDigitAfterDecimalPoint);
            numberOfDigitAfterDecimalPointTxt += StringUtils.repeat("0", numberOfDigitAfterDecimalPoint);
        }
        DecimalFormat df = new DecimalFormat(pattern);
        String totalValue = df.format(value.doubleValue());
        String split[] = totalValue.split("\\.");

        String excludedDecimalPointValue = split[0];
        if (split[1] != null) {
            excludedDecimalPointValue += split[1];
        }
        //if division contained reminder
        if(Integer.parseInt(excludedDecimalPointValue) % roundNearestTo != 0) {
            Integer divisionResult = (int) Double.parseDouble(excludedDecimalPointValue) / roundNearestTo;
            Integer roundedValue;
            if (roundingType.intValue() == RoundingType.RoundDown.get()) {
                roundedValue = divisionResult * roundNearestTo;
            } else {
                roundedValue = (divisionResult + 1) * roundNearestTo;
            }
            calculatedAmount = roundedValue/Double.parseDouble(numberOfDigitAfterDecimalPointTxt);
        }else {
            return value;
        }
        return calculatedAmount;
    }
}
