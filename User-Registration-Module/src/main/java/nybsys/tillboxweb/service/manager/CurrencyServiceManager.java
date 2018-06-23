/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/9/2018
 * Time: 9:48 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.service.manager;

import nybsys.tillboxweb.BaseService;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.bll.manager.CurrencyBllManager;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreModels.CurrencyModel;
import nybsys.tillboxweb.models.VMCurrencyExchangeRateRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CurrencyServiceManager extends BaseService {

    private static final Logger log = LoggerFactory.getLogger(Core.class);
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    @Autowired
    private CurrencyBllManager currencyBllManager;

    public ResponseMessage save(RequestMessage requestMessage) {
        this.currencyBllManager = new CurrencyBllManager();
        ResponseMessage responseMessage = new ResponseMessage();
        CurrencyModel currencyModel = new CurrencyModel();
        try {
            if (requestMessage.businessID == null || requestMessage.businessID == 0) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.SELECT_A_BUSINESS;
                Core.clientMessage.get().userMessage = MessageConstant.SELECT_A_BUSINESS;
                return responseMessage;
            }

            currencyModel = Core.getRequestObject(requestMessage, CurrencyModel.class);
            currencyModel.setBusinessID(requestMessage.businessID);
            
            /* Set<ConstraintViolation<CurrencyModel>> violations = this.validator.validate(currencyModel);
             for (ConstraintViolation<BusinessModel> violation : violations) {
                 log.error(violation.getMessage());
             }*/

            currencyModel = this.currencyBllManager.saveOrUpdate(currencyModel);

            responseMessage.responseObj = currencyModel;
            if (Core.clientMessage.get().messageCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.CURRENCY_SAVE_SUCCESSFULLY;
                this.commit();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage != null) {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                } else {
                    responseMessage.message = MessageConstant.CURRENCY_SAVE_FAILED;
                }
                this.rollBack();
            }
        } catch (Exception ex) {
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, MessageConstant.OPERATION_FAILED, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
            this.WriteExceptionLog(ex);
            this.rollBack();
            log.error("CurrencyServiceManager -> save got exception");

        }
        return responseMessage;
    }

    public ResponseMessage changeCurrentCurrency(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        this.currencyBllManager = new CurrencyBllManager();
        CurrencyModel currencyModel = new CurrencyModel();
        try {

            if (requestMessage.userID == null || requestMessage.businessID == null || requestMessage.businessID == 0 || requestMessage.userID == "") {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.SELECT_A_BUSINESS;
                Core.clientMessage.get().userMessage = MessageConstant.SELECT_A_BUSINESS;
                return responseMessage;
            }

            currencyModel = Core.getRequestObject(requestMessage, CurrencyModel.class);

            if (this.currencyBllManager.changeCurrentCurrency(requestMessage.userID, requestMessage.businessID, currencyModel.getCurrencyID())) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.CURRENCY_CHANGE_SUCCESSFULLY;
                this.commit();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.CURRENCY_CHANGE_FAILED;
                this.rollBack();
            }

            responseMessage.responseObj = currencyModel;

        } catch (Exception ex) {
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, MessageConstant.OPERATION_FAILED, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
            this.WriteExceptionLog(ex);
            log.error("CurrencyServiceManager -> changeCurrentCurrency got exception");
            this.rollBack();

        }
        return responseMessage;
    }


    public ResponseMessage getBaseCurrency(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        this.currencyBllManager = new CurrencyBllManager();
        CurrencyModel currencyModel;
        try {

            if (requestMessage.businessID == null || requestMessage.businessID == 0) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.SELECT_A_BUSINESS;
                Core.clientMessage.get().userMessage = MessageConstant.SELECT_A_BUSINESS;
                return responseMessage;
            }

            currencyModel = this.currencyBllManager.getBaseCurrency(requestMessage.businessID);
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.CURRENCY_GET_FAILED;
                return responseMessage;
            }

            responseMessage.responseObj = currencyModel;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            responseMessage.message = MessageConstant.CURRENCY_GET_SUCCESSFULLY;

        } catch (Exception ex) {
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, MessageConstant.OPERATION_FAILED, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
            this.WriteExceptionLog(ex);
            log.error("CurrencyServiceManager -> getCurrentCurrency got exception");

        }
        return responseMessage;
    }

    public ResponseMessage search(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        this.currencyBllManager = new CurrencyBllManager();
        CurrencyModel currencyModel = new CurrencyModel();
        List<CurrencyModel> lstCurrencyModel;
        try {
            currencyModel = Core.getRequestObject(requestMessage, CurrencyModel.class);
            if (currencyModel == null) {
                currencyModel = new CurrencyModel();
            }
            if (currencyModel.getBusinessID() == null) {
                currencyModel.setBusinessID(requestMessage.businessID);
            }
            if (currencyModel.getStatus() == null) {
                currencyModel.setStatus(TillBoxAppEnum.Status.Active.get());
            }

            lstCurrencyModel = this.currencyBllManager.getAllByConditions(currencyModel);
            responseMessage.responseObj = lstCurrencyModel;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;

        } catch (Exception ex) {
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, MessageConstant.OPERATION_FAILED, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
            this.WriteExceptionLog(ex);
            log.error("CurrencyServiceManager -> search got exception");
        }
        return responseMessage;
    }

    public ResponseMessage getCurrencyExchangeRate(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        this.currencyBllManager = new CurrencyBllManager();
        List<CurrencyModel> lstCurrencyModel;
        VMCurrencyExchangeRateRequestModel vmCurrencyExchangeRateRequestModel;
        try {

            vmCurrencyExchangeRateRequestModel = Core.getRequestObject(requestMessage, VMCurrencyExchangeRateRequestModel.class);

            lstCurrencyModel = this.currencyBllManager.getExchangeRate(requestMessage.businessID, vmCurrencyExchangeRateRequestModel);

            responseMessage.responseObj = lstCurrencyModel;
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.message = Core.clientMessage.get().message;
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            }

        } catch (Exception ex) {
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, MessageConstant.OPERATION_FAILED, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
            this.WriteExceptionLog(ex);
            log.error("CurrencyServiceManager -> getExchangeRate got exception");
        }
        return responseMessage;
    }

    public ResponseMessage getAllCurrencyExchangeRate(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        this.currencyBllManager = new CurrencyBllManager();
        List<CurrencyModel> lstCurrencyModel;
        try {

            lstCurrencyModel = this.currencyBllManager.getAllExchangeRate(requestMessage.businessID);

            responseMessage.responseObj = lstCurrencyModel;
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.message = Core.clientMessage.get().message;
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            }

        } catch (Exception ex) {
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, MessageConstant.OPERATION_FAILED, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
            this.WriteExceptionLog(ex);
            log.error("CurrencyServiceManager -> getExchangeRate got exception");
        }
        return responseMessage;
    }

}