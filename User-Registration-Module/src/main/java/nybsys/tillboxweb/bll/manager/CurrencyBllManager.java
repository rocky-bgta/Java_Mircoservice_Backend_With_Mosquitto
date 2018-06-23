/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/9/2018
 * Time: 10:06 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.bll.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nybsys.tillboxweb.BaseBll;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreEntities.Currency;
import nybsys.tillboxweb.coreModels.CurrencyExchangeRateModel;
import nybsys.tillboxweb.coreModels.CurrencyModel;
import nybsys.tillboxweb.models.CurrencySettingModel;
import nybsys.tillboxweb.models.VMCurrencyExchangeRateRequestModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CurrencyBllManager extends BaseBll<Currency> {

    private static final Logger log = LoggerFactory.getLogger(CurrencyBllManager.class);

    @Autowired
    SessionBllManager sessionBllManager = new SessionBllManager();

    @Autowired
    private CurrencySettingBllManager currencySettingBllManager = new CurrencySettingBllManager();

    //@Autowired
    //private CurrencyExchangeRateBllManager currencyExchangeRateBllManager = new CurrencyExchangeRateBllManager();

    @Override
    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(Currency.class);
        Core.runTimeModelType.set(CurrencyModel.class);
    }

    public CurrencyModel saveOrUpdate(CurrencyModel currencyModelReq) throws Exception {
        CurrencyModel currencyModel;
        try {
            currencyModel = currencyModelReq;
            //save
            if (currencyModel.getCurrencyID() == null || currencyModel.getCurrencyID() == 0) {
                currencyModel = this.save(currencyModel);
                if (currencyModel == null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = MessageConstant.CURRENCY_SAVE_FAILED;
                }
            } else {
                CurrencyModel baseCurrencyModel = new CurrencyModel();
                baseCurrencyModel = this.getBaseCurrency(currencyModel.getBusinessID());
                if(baseCurrencyModel == null){
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = MessageConstant.CURRENCY_GET_FAILED;
                    return currencyModel;
                }

                if(baseCurrencyModel.getCurrencyID() == currencyModel.getCurrencyID()){
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = MessageConstant.BASE_CURRENCY_CAN_NOT_MODIFY;
                    Core.clientMessage.get().userMessage = MessageConstant.BASE_CURRENCY_CAN_NOT_MODIFY;
                }else {
                    currencyModel = this.update(currencyModel);
                    if (currencyModel == null) {
                        Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                        Core.clientMessage.get().message = MessageConstant.CURRENCY_UPDATE_FAILED;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("CurrencyBllManager -> saveOrUpdate got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return currencyModel;
    }

    public boolean changeCurrentCurrency(String userID, Integer businessID, Integer currencyID) throws Exception {
        try {

            if (this.sessionBllManager.changeCurrentCurrency(userID, businessID, currencyID)) {
                return true;
            }

        } catch (Exception ex) {
            log.error("CurrencyBllManager -> changeCurrentCurrency got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return false;
    }

    public CurrencyModel getBaseCurrency(Integer businessID) throws Exception {
        List<CurrencyModel> lstCurrencyModel = new ArrayList<>();
        CurrencyModel currencyModel = new CurrencyModel();
        try {
            currencyModel.setBusinessID(businessID);
            currencyModel.setBaseCurrency(true);

            lstCurrencyModel = this.getAllByConditionWithActive(currencyModel);
            if (lstCurrencyModel.size() == 0) {
                currencyModel = null;
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                Core.clientMessage.get().message = MessageConstant.CURRENCY_GET_FAILED;
            } else {
                currencyModel = lstCurrencyModel.get(0);
            }

        } catch (Exception ex) {
            log.error("CurrencyBllManager -> getBaseCurrency got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return currencyModel;
    }

    public List<CurrencyModel> getAllCurrencyByBusinessID(Integer businessID) throws Exception {
        List<CurrencyModel> lstCurrencyModel = new ArrayList<>();
        CurrencyModel currencyModel = new CurrencyModel();
        try {
            currencyModel.setBusinessID(businessID);

            lstCurrencyModel = this.getAllByConditionWithActive(currencyModel);
            if (lstCurrencyModel.size() == 0) {
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                Core.clientMessage.get().message = MessageConstant.CURRENCY_GET_FAILED;
            }

        } catch (Exception ex) {
            log.error("CurrencyBllManager -> getAllCurrencyByBusinessID got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return lstCurrencyModel;
    }

    public List<CurrencyModel> getExchangeRate(Integer businessID, VMCurrencyExchangeRateRequestModel vmCurrencyExchangeRateRequestModel) throws Exception {
        List<CurrencyModel> lstCurrencyModel = new ArrayList<>();
        CurrencyModel baseCurrencyModel;
        CurrencyModel entryCurrencyModel;
        CurrencyExchangeRateModel currencyExchangeRateModel = new CurrencyExchangeRateModel();
        CurrencySettingModel currencySettingModel;
        try {

            baseCurrencyModel = this.getBaseCurrency(businessID);
            //base currency not found
            if (Core.clientMessage.get().messageCode != null) {
                Core.clientMessage.get().userMessage = MessageConstant.CURRENCY_GET_FAILED;
                return lstCurrencyModel;
            }

            lstCurrencyModel.add(baseCurrencyModel);

            // if base currency and entry currency are not same
            if (baseCurrencyModel.getCurrencyID() != vmCurrencyExchangeRateRequestModel.getPreferredCurrencyID()) {

                //currencyExchangeRateModel = this.currencyExchangeRateBllManager.getExchangeRateByCurrencyID(vmCurrencyExchangeRateRequestModel.getPreferredCurrencyID(), vmCurrencyExchangeRateRequestModel.getTransactionDate());

                // if not found exchange rate
                if (currencyExchangeRateModel == null) {

                    // check user is using live api or not
                    currencySettingModel = this.currencySettingBllManager.getCurrencySetting(businessID);

                    if (currencySettingModel != null && currencySettingModel.getUseLiveExchangeRate()) {

                        // call live api and update currency rate
                        this.setExchangeRate(baseCurrencyModel.getCurrencyCode(), vmCurrencyExchangeRateRequestModel.getTransactionDate(), businessID);

                        //get entry currency model
                        entryCurrencyModel = this.getById(vmCurrencyExchangeRateRequestModel.getPreferredCurrencyID(), TillBoxAppEnum.Status.Active.get());
                        if (entryCurrencyModel == null) {
                            Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                            Core.clientMessage.get().userMessage = MessageConstant.CURRENCY_GET_FAILED;
                            return lstCurrencyModel;

                        }

                        // get entry currency exchange rate

                        //currencyExchangeRateModel = this.currencyExchangeRateBllManager.getExchangeRateByCurrencyID(vmCurrencyExchangeRateRequestModel.getPreferredCurrencyID(), vmCurrencyExchangeRateRequestModel.getTransactionDate());
                        if (currencyExchangeRateModel == null) {
                            Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                            Core.clientMessage.get().userMessage = MessageConstant.CURRENCY_EXCHANGE_RATE_GET_FAILED;
                            return lstCurrencyModel;

                        }

                        entryCurrencyModel.setExchangeRate(currencyExchangeRateModel.getRate());

                        lstCurrencyModel.add(entryCurrencyModel);

                    } else {
                        // give 0.0 as exchangeRate
                        //get entry currency model
                        entryCurrencyModel = this.getById(vmCurrencyExchangeRateRequestModel.getPreferredCurrencyID(), TillBoxAppEnum.Status.Active.get());
                        if (entryCurrencyModel == null) {
                            Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                            Core.clientMessage.get().userMessage = MessageConstant.CURRENCY_GET_FAILED;
                            return lstCurrencyModel;

                        }
                        entryCurrencyModel.setExchangeRate(0.0);
                        lstCurrencyModel.add(entryCurrencyModel);
                    }
                } else {
                    entryCurrencyModel = this.getById(currencyExchangeRateModel.getCurrencyID(), TillBoxAppEnum.Status.Active.get());
                    entryCurrencyModel.setExchangeRate(currencyExchangeRateModel.getRate());
                    lstCurrencyModel.add(entryCurrencyModel);
                }
            }

        } catch (Exception ex) {
            log.error("CurrencyBllManager -> getExchangeRate got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return lstCurrencyModel;
    }

    public List<CurrencyModel> getAllExchangeRate(Integer businessID) throws Exception {
        List<CurrencyModel> lstCurrencyModel = new ArrayList<>();
        CurrencyModel baseCurrencyModel;
        CurrencyModel currencyModel = new CurrencyModel();
        CurrencyExchangeRateModel currencyExchangeRateModel = new CurrencyExchangeRateModel();
        List<CurrencyExchangeRateModel> lstCurrencyExchangeRateModel = new ArrayList<>();
        try {

            baseCurrencyModel = this.getBaseCurrency(businessID);
            //base currency not found
            if (Core.clientMessage.get().messageCode != null) {
                Core.clientMessage.get().userMessage = MessageConstant.CURRENCY_GET_FAILED;
                return lstCurrencyModel;
            }

            //get all currency of this business
            currencyModel.setBusinessID(businessID);
            lstCurrencyModel = this.getAllByConditionWithActive(currencyModel);

            if (lstCurrencyModel.size() > 0) {

                // get today exchange rate
                lstCurrencyExchangeRateModel = getLiveExchangeRate(baseCurrencyModel.getCurrencyCode(), businessID);
                if(lstCurrencyExchangeRateModel.size() == 0){
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().userMessage = MessageConstant.CURRENCY_EXCHANGE_RATE_GET_FAILED;
                    return lstCurrencyModel;
                }else{

                    for (int index = 0; index < lstCurrencyModel.size(); index++) {

                        for (CurrencyExchangeRateModel liveCurrencyExchangeRateModel:lstCurrencyExchangeRateModel){
                            if(liveCurrencyExchangeRateModel.getCurrencyID() == lstCurrencyModel.get(index).getCurrencyID()){
                                lstCurrencyModel.get(index).setExchangeRate(liveCurrencyExchangeRateModel.getRate());
                                break;
                            }
                        }

                    }
                }
            }

        } catch (Exception ex) {
            log.error("CurrencyBllManager -> getAllExchangeRate got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return lstCurrencyModel;
    }

    private void setExchangeRate(String baseCurrencyCode, Date transactionDate, Integer businessID) throws Exception {
        StringBuilder result = new StringBuilder();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
        Date today = new Date();
        URL url;

        //if today
        if (formatter.format(transactionDate).equals(formatter.format(today))) {
            url = new URL(TillBoxAppConstant.CURRENCY_EXCHANGE_RATE_LIVE_API + "latest" + TillBoxAppConstant.CURRENCY_EXCHANGE_RATE_LIVE_API_KEY);
        } else {
            url = new URL(TillBoxAppConstant.CURRENCY_EXCHANGE_RATE_LIVE_API + formatter.format(transactionDate) + TillBoxAppConstant.CURRENCY_EXCHANGE_RATE_LIVE_API_KEY);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();

        int ratesStartIndex = StringUtils.ordinalIndexOf(result, "{", 2);
        int ratesEndIndex = StringUtils.ordinalIndexOf(result, "}", 1) + 1;
        String ratesSting = result.substring(ratesStartIndex, ratesEndIndex);

//        HashMap<String, Object> excludeRatesMap = new HashMap<String, Object>();
        HashMap<String, Object> ratesMap = new HashMap<String, Object>();
        ObjectMapper mapper = new ObjectMapper();

//        //Convert other property exclude rates json
//        excludeRatesMap = mapper.readValue(result.toString(), new TypeReference<Map<String, Object>>() {
//        });
//        for (String key : excludeRatesMap.keySet()) {
//            //add  excludes rates
//            if (key == "base") {
//                liveExchangeRateModel.setBaseCurrencyCode(excludeRatesMap.get(key).toString());
//            } else if (key == "date") {
//                liveExchangeRateModel.setDate(new Date(excludeRatesMap.get(key).toString()));
//            }
//        }

        //Convert rates json
        ratesMap = mapper.readValue(ratesSting, new TypeReference<Map<String, Object>>() {
        });

        List<CurrencyModel> lstCurrencyModel = new ArrayList<>();
        CurrencyModel currencyModel = new CurrencyModel();

        //update currency exchange rate table
        for (String key : ratesMap.keySet()) {

            //get currency by code
            currencyModel.setCurrencyCode(key);
            currencyModel.setBusinessID(businessID);

            lstCurrencyModel = this.getAllByConditionWithActive(currencyModel);
            if (lstCurrencyModel.size() > 0) {
                //save today exchange rate
                CurrencyExchangeRateModel currencyExchangeRateModel = new CurrencyExchangeRateModel();
                currencyExchangeRateModel.setDate(new Date());
                currencyExchangeRateModel.setCurrencyID(lstCurrencyModel.get(0).getCurrencyID());
                currencyExchangeRateModel.setRate(((Number)ratesMap.get(key)).doubleValue());
                currencyExchangeRateModel.setBusinessID(businessID);

                //this.currencyExchangeRateBllManager.save(currencyExchangeRateModel);
            }
        }
    }

    private List<CurrencyExchangeRateModel> getLiveExchangeRate(String baseCurrencyCode, Integer businessID) throws Exception {
        StringBuilder result = new StringBuilder();
        List<CurrencyExchangeRateModel> lstCurrencyExchangeRateModel = new ArrayList<>();

        URL url = new URL(TillBoxAppConstant.CURRENCY_EXCHANGE_RATE_LIVE_API + "latest" + TillBoxAppConstant.CURRENCY_EXCHANGE_RATE_LIVE_API_KEY);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();

        int ratesStartIndex = StringUtils.ordinalIndexOf(result, "{", 2);
        int ratesEndIndex = StringUtils.ordinalIndexOf(result, "}", 1) + 1;
        String ratesSting = result.substring(ratesStartIndex, ratesEndIndex);

        HashMap<String, Object> ratesMap = new HashMap<String, Object>();
        ObjectMapper mapper = new ObjectMapper();

        //Convert rates json
        ratesMap = mapper.readValue(ratesSting, new TypeReference<Map<String, Object>>() {
        });

        List<CurrencyModel> lstCurrencyModel = new ArrayList<>();
        CurrencyModel currencyModel = new CurrencyModel();

        //update currency exchange rate table
        for (String key : ratesMap.keySet()) {

            //get currency by code
            currencyModel.setCurrencyCode(key);
            currencyModel.setBusinessID(businessID);

            lstCurrencyModel = this.getAllByConditionWithActive(currencyModel);
            if (lstCurrencyModel.size() > 0) {
                //save today exchange rate
                CurrencyExchangeRateModel currencyExchangeRateModel = new CurrencyExchangeRateModel();
                currencyExchangeRateModel.setDate(new Date());
                currencyExchangeRateModel.setCurrencyID(lstCurrencyModel.get(0).getCurrencyID());
                currencyExchangeRateModel.setRate(((Number)ratesMap.get(key)).doubleValue());
                currencyExchangeRateModel.setBusinessID(businessID);
                lstCurrencyExchangeRateModel.add(currencyExchangeRateModel);
            }
        }
        return lstCurrencyExchangeRateModel;
    }
}
