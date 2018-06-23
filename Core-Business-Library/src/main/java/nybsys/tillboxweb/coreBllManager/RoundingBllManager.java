/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 19-Apr-18
 * Time: 12:39 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.coreBllManager;

import nybsys.tillboxweb.BaseBll;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreConstant.CompanySettingConstant;
import nybsys.tillboxweb.coreEntities.Rounding;
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
public class RoundingBllManager extends BaseBll<Rounding> {

    private static final Logger log = LoggerFactory.getLogger(RoundingBllManager.class);

    @Override
    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(Rounding.class);
        Core.runTimeModelType.set(RoundingModel.class);
    }

    public RoundingModel saveOrUpdate(RoundingModel roundingModelReq) throws Exception {
        RoundingModel roundingModel = new RoundingModel();
        try {
            roundingModel = roundingModelReq;

            //save
            if (roundingModel.getRoundingID() == null || roundingModel.getRoundingID() == 0) {
                roundingModel = this.save(roundingModel);
                if (roundingModel == null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = CompanySettingConstant.ROUNDING_SAVE_FAILED;
                }

            } else {
                roundingModel = this.update(roundingModel);
                if (roundingModel == null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = CompanySettingConstant.ROUNDING_UPDATE_FAILED;
                }
            }
        } catch (Exception ex) {
            log.error("RoundingBllManager -> saveOrUpdate got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return roundingModel;
    }

    public RoundingModel getRoundingSetting(Integer businessID) throws Exception {
        RoundingModel roundingModel = new RoundingModel();
        List<RoundingModel> lstRoundingModel = new ArrayList<>();
        try {
            roundingModel.setBusinessID(businessID);

            lstRoundingModel = this.getAllByConditionWithActive(roundingModel);
            if(lstRoundingModel.size()>0){
                roundingModel = lstRoundingModel.get(0);
            }else{
                roundingModel = null;
            }
        } catch (Exception ex) {
            log.error("RoundingBllManager -> getRoundingSetting got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return roundingModel;
    }
}