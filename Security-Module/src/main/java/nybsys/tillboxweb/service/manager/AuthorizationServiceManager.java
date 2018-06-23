/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 5/17/2018
 * Time: 10:22 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.service.manager;

import nybsys.tillboxweb.Authorization;
import nybsys.tillboxweb.BaseService;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.MessageModel.SecurityReqMessage;
import nybsys.tillboxweb.MessageModel.SecurityResMessage;

public class AuthorizationServiceManager extends BaseService {

    private Authorization authorization = new Authorization();

    public ResponseMessage authorizeUser(RequestMessage requestMessage) {
        ResponseMessage responseMessage = this.getDefaultResponseMessage();
        SecurityResMessage securityResMessage;
        SecurityReqMessage securityReqMessage;// = new SecurityReqMessage();

      /*  securityReqMessage.messageId = requestMessage.brokerMessage.messageId;
        securityReqMessage.serviceUrl = requestMessage.brokerMessage.serviceName;
        securityReqMessage.token = requestMessage.token;*/

        try {

           securityReqMessage = Core.getRequestObject(requestMessage, SecurityReqMessage.class);
           securityResMessage  = authorization.getUsersPermission(securityReqMessage);
           responseMessage.responseObj = securityResMessage;

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return responseMessage;
    }
}
