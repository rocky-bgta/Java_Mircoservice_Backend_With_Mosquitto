/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 23/02/2018
 * Time: 10:15
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;
import nybsys.tillboxweb.coreModels.OpeningBalanceModel;
import nybsys.tillboxweb.coreModels.RememberNoteModel;
import nybsys.tillboxweb.coreModels.UserDefineSettingDetailModel;

import java.util.ArrayList;
import java.util.List;

public class VMSupplierModel  {
    public SupplierModel supplierModel = new SupplierModel();
    public OpeningBalanceModel openingBalanceModel = new OpeningBalanceModel();
    public List<SupplierAddressModel> lstSupplierAddressModel = new ArrayList<>();
    public List<SupplierContactModel> lstSupplierContactModel = new ArrayList<>();

    public BankingDetailsModel bankingDetailsModel = new BankingDetailsModel();
   // public VMRememberNoteModel vmRememberNoteModel = new VMRememberNoteModel();
    public VMUserDetailSettingDetailModel vmUserDetailSettingDetailModel = new VMUserDetailSettingDetailModel();
    public ReportingLayoutModel reportingLayoutModel = new ReportingLayoutModel();

    public List<UserDefineSettingDetailModel> lstUserDefineSettingDetailModels;
    public List<RememberNoteModel> lstRememberNoteModels;
}
