/**
 * Created By: Md. Rashed Khan Menon
 * Created Date:17:05:2018
 * Time: 5:40 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.service.manager;

import nybsys.tillboxweb.BaseService;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreBllManager.RoundingBllManager;
import nybsys.tillboxweb.coreConstant.CompanySettingConstant;
import nybsys.tillboxweb.coreModels.RoundingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RoundingServiceManager extends BaseService {

    private static final Logger log = LoggerFactory.getLogger(RoundingServiceManager.class);

    //@Autowired
    private RoundingBllManager roundingBllManager = new RoundingBllManager();

    public ResponseMessage saveOrUpdate(RequestMessage requestMessage) {
        ResponseMessage responseMessage = this.getDefaultResponseMessage();
        RoundingModel roundingModel;
        try {

            roundingModel = Core.getRequestObject(requestMessage, RoundingModel.class);
            roundingModel.setBusinessID(requestMessage.businessID);

            roundingModel = this.roundingBllManager.saveOrUpdate(roundingModel);

            responseMessage.responseObj = roundingModel;
            if (Core.clientMessage.get().messageCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = CompanySettingConstant.ROUNDING_SAVE_SUCCESSFULLY;
                this.commit();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage != null) {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                } else {
                    responseMessage.message = CompanySettingConstant.ROUNDING_SAVE_FAILED;
                }
                this.rollBack();
            }
        } catch (Exception ex) {
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, MessageConstant.OPERATION_FAILED, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
            this.WriteExceptionLog(ex);
            this.rollBack();
            log.error("RoundingServiceManager -> saveOrUpdate got exception");
        }
        return responseMessage;
    }

    public ResponseMessage search(RequestMessage requestMessage) {
        ResponseMessage responseMessage = this.getDefaultResponseMessage();
        RoundingModel roundingModel = new RoundingModel();
        try {
            roundingModel.setBusinessID(requestMessage.businessID);

            roundingModel = this.roundingBllManager.getRoundingSetting(requestMessage.businessID);
            if (roundingModel != null) {
                responseMessage.responseObj = roundingModel;
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = CompanySettingConstant.ROUNDING_GET_FAILED;
            }
        } catch (Exception ex) {
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, MessageConstant.OPERATION_FAILED, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
            this.WriteExceptionLog(ex);
            log.error("RoundingServiceManager -> search got exception");
        }
        return responseMessage;
    }

}