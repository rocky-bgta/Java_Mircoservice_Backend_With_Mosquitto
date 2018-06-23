/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 22-Dec-17
 * Time: 10:50 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */
package nybsys.tillboxweb.controller;


import nybsys.tillboxweb.BaseController;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.service.manager.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ApiRouter extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ApiRouter.class);

    /*@Autowired
    private TaskExecutor taskExecutor;*/

    //@Autowired
    private AccountServiceManager accountServiceManager;// = new AccountServiceManager();

    //@Autowired
    private BudgetServiceManager budgetServiceManager;// = new BudgetServiceManager();

    //@Autowired
    private MoneyTransferServiceManager moneyTransferServiceManager;// = new MoneyTransferServiceManager();

    //@Autowired
    private CombineAccountServiceManager combineAccountServiceManager;// = new CombineAccountServiceManager();

    //@Autowired
    private OpeningBalanceServiceManager openingBalanceServiceManager;// = new OpeningBalanceServiceManager();

    //@Autowired
    private FinancialYearServiceManager financialYearServiceManager;// = new FinancialYearServiceManager();

    //@Autowired
    private JournalServiceManager journalServiceManager;// = new JournalServiceManager();


    @Override
    public ResponseMessage getResponseMessage(String serviceName, RequestMessage requestMessage) {
        this.checkSecurityAndExecuteService(serviceName,requestMessage);
        return this.responseMessage;
    }

    protected void executeServiceManager(String serviceName, RequestMessage requestMessage) {
        switch (serviceName) {

            case "api/financialYear/save":
                this.financialYearServiceManager = new FinancialYearServiceManager();
                this.responseMessage = this.financialYearServiceManager.save(requestMessage);
                log.info("account module -> api/financialYear/save executed");
                this.financialYearServiceManager=null;
                break;

            case "api/financialYear/get":
                this.financialYearServiceManager = new FinancialYearServiceManager();
                this.responseMessage = this.financialYearServiceManager.getAllFinancialYear(requestMessage);
                log.info("account module -> api/financialYear/get executed");
                this.financialYearServiceManager=null;
                break;

            case "api/financialYear/current/get":
                this.financialYearServiceManager = new FinancialYearServiceManager();
                this.responseMessage = this.financialYearServiceManager.getCurrentFinancialYearByBusinessID(requestMessage);
                log.info("account module -> api/financialYear/Current/get executed");
                this.financialYearServiceManager=null;
                break;

            case "api/financialYear/search":
                this.financialYearServiceManager = new FinancialYearServiceManager();
                this.responseMessage = this.financialYearServiceManager.search(requestMessage);
                log.info("account module -> api/financialYear/search executed");
                this.financialYearServiceManager=null;
                break;

            case "api/journal/save":
                this.journalServiceManager = new JournalServiceManager();
                this.responseMessage = this.journalServiceManager.saveJournal(requestMessage);
                log.info("account module -> api/journal/save executed");
                this.journalServiceManager=null;
                break;

            case "api/journal/bypassEntryJournal/save":
                this.journalServiceManager = new JournalServiceManager();
                this.responseMessage = this.journalServiceManager.saveBypassEntryJournal(requestMessage);
                log.info("account module -> api/journal/bypassEntryJournal/save executed");
                this.journalServiceManager=null;
                break;

            case "api/journal/availableBalanceByAccount/get":
                this.journalServiceManager = new JournalServiceManager();
                this.responseMessage = this.journalServiceManager.getAvailableBalanceByAccount(requestMessage);
                log.info("account module -> api/journal/availableBalanceByAccount/get executed");
                this.journalServiceManager=null;
                break;

            case "api/journal/availableBalanceByPartyID/get":
                this.journalServiceManager = new JournalServiceManager();
                this.responseMessage = this.journalServiceManager.getAvailableBalancePartyID(requestMessage);
                log.info("account module -> api/journal/availableBalanceByPartyID/get executed");
                this.journalServiceManager=null;
                break;

            case "api/journal/supplierCurrentDue/get":
                this.journalServiceManager = new JournalServiceManager();
                this.responseMessage = this.journalServiceManager.getSupplierCurrentDue(requestMessage);
                log.info("account module -> api/journal/supplierCurrentDue/get executed");
                this.journalServiceManager=null;
                break;

            case "api/journal/customerCurrentDue/get":
                this.journalServiceManager = new JournalServiceManager();
                this.responseMessage = this.journalServiceManager.getCustomerCurrentDue(requestMessage);
                log.info("account module -> api/journal/customerCurrentDue/get executed");
                this.journalServiceManager=null;
                break;

            case "api/journal/search":
                this.journalServiceManager = new JournalServiceManager();
                this.responseMessage = this.journalServiceManager.search(requestMessage);
                log.info("account module -> api/journal/search executed");
                this.journalServiceManager=null;
                break;

            case "api/journal/delete":
                this.journalServiceManager = new JournalServiceManager();
                this.responseMessage = this.journalServiceManager.delete(requestMessage);
                log.info("account module -> api/journal/delete executed");
                this.journalServiceManager=null;
                break;

            case "api/journal/dataExistsExcludeOpeningBalance":
                this.journalServiceManager = new JournalServiceManager();
                this.responseMessage = this.journalServiceManager.dataExistsExcludeOpeningBalance(requestMessage);
                log.info("account module -> api/journal/dataExists executed");
                this.journalServiceManager=null;
                break;

            case "api/account/dropDownList":
                this.accountServiceManager = new AccountServiceManager();
                this.responseMessage = this.accountServiceManager.getAccountDropDownList(requestMessage);
                log.info("account module -> api/account/dropDownList executed");
                this.accountServiceManager=null;
                break;

            case "api/account/accountClassification/get":
                this.accountServiceManager = new AccountServiceManager();
                this.responseMessage = this.accountServiceManager.getAccountClassificationList(requestMessage);
                log.info("account module -> api/account/accountClassification/get executed");
                this.accountServiceManager=null;
                break;

            case "api/account/save":
                this.accountServiceManager = new AccountServiceManager();
                this.responseMessage = this.accountServiceManager.saveAccount(requestMessage);
                log.info("account module -> api/account/save executed");
                this.accountServiceManager=null;
                break;

            case "api/account/root/get":
                this.accountServiceManager = new AccountServiceManager();
                this.responseMessage = this.accountServiceManager.getRootAccount(requestMessage);
                log.info("account module -> api/account/root/get executed");
                this.accountServiceManager=null;
                break;

            case "api/account/get":
                this.accountServiceManager = new AccountServiceManager();
                this.responseMessage = this.accountServiceManager.getAccount(requestMessage);
                log.info("account module -> api/account/get executed");
                this.accountServiceManager=null;
                break;

            case "api/account/parentAccountForSupplierPayment":
                this.accountServiceManager = new AccountServiceManager();
                this.responseMessage = this.accountServiceManager.parentAccountForSupplierPayment(requestMessage);
                log.info("account module -> api/account/parentAccountForSupplierPayment executed");
                this.accountServiceManager=null;
                break;

            case "api/account/deActive/get":
                this.accountServiceManager = new AccountServiceManager();
                this.responseMessage = this.accountServiceManager.getDeActiveAccount(requestMessage);
                log.info("account module -> api/account/deActive/get executed");
                this.accountServiceManager=null;
                break;

            case "api/account/withOpeningBalance/get":
                this.accountServiceManager = new AccountServiceManager();
                this.responseMessage = this.accountServiceManager.getAllAccountWithOpeningBalance(requestMessage);
                log.info("account module -> api/account/withOpeningBalance/get executed");
                this.accountServiceManager=null;
                break;

            case "api/moneyTransfer/save":
                this.moneyTransferServiceManager = new MoneyTransferServiceManager();
                this.responseMessage = this.moneyTransferServiceManager.saveMoneyTransfer(requestMessage);
                log.info("account module -> api/moneyTransfer/save executed");
                this.moneyTransferServiceManager=null;
                break;

            case "api/moneyTransfer/get":
                this.moneyTransferServiceManager = new MoneyTransferServiceManager();
                this.responseMessage = this.moneyTransferServiceManager.getAllMoneyTransfer(requestMessage);
                log.info("account module -> api/moneyTransfer/get executed");
                this.moneyTransferServiceManager=null;
                break;

            case "api/budget/dropDownList/get":
                this.budgetServiceManager = new BudgetServiceManager();
                this.responseMessage = this.budgetServiceManager.getBudgetDropDown(requestMessage);
                log.info("account module -> api/budget/dropDownList/get executed");
                this.budgetServiceManager=null;
                break;

            case "api/budget/detail/get":
                this.budgetServiceManager = new BudgetServiceManager();
                this.responseMessage = this.budgetServiceManager.getBudgetDetail(requestMessage);
                log.info("account module -> api/budget/detail/get executed");
                this.budgetServiceManager=null;
                break;

            case "api/budget/detail/save":
                this.budgetServiceManager = new BudgetServiceManager();
                this.responseMessage = this.budgetServiceManager.saveBudgetDetail(requestMessage);
                log.info("account module -> api/budget/detail/save executed");
                this.budgetServiceManager=null;
                break;

            case "api/combineAccount/save":
                this.combineAccountServiceManager = new CombineAccountServiceManager();
                this.responseMessage = this.combineAccountServiceManager.saveCombineAccount(requestMessage);
                log.info("account module -> api/combineAccount/save executed");
                this.combineAccountServiceManager=null;
                break;

            case "api/combineAccount/search":
                this.combineAccountServiceManager = new CombineAccountServiceManager();
                this.responseMessage = this.combineAccountServiceManager.search(requestMessage);
                log.info("account module -> api/combineAccount/save executed");
                this.combineAccountServiceManager=null;
                break;

            case "api/combineAccount/getByID":
                this.combineAccountServiceManager = new CombineAccountServiceManager();
                this.responseMessage = this.combineAccountServiceManager.getByBusinessID(requestMessage);
                log.info("account module -> api/combineAccount/getByID executed");
                this.combineAccountServiceManager=null;
                break;

            case "api/openingBalance/save":
                this.openingBalanceServiceManager = new OpeningBalanceServiceManager();
                this.responseMessage = this.openingBalanceServiceManager.save(requestMessage);
                log.info("account module -> api/openingBalance/save executed");
                this.openingBalanceServiceManager=null;
                break;
            case "api/openingBalance/search":
                this.openingBalanceServiceManager = new OpeningBalanceServiceManager();
                this.responseMessage = this.openingBalanceServiceManager.searchOpeningBalance(requestMessage);
                log.info("account module -> api/openingBalance/save executed");
                this.openingBalanceServiceManager=null;
                break;
            case "api/openingBalance/item/save":
                this.openingBalanceServiceManager = new OpeningBalanceServiceManager();
                this.responseMessage = this.openingBalanceServiceManager.itemOpeningBalanceSave(requestMessage);
                log.info("account module -> api/openingBalance/item/save executed");
                this.openingBalanceServiceManager=null;
                break;

            case "api/openingBalance/getByAccountID":
                this.openingBalanceServiceManager = new OpeningBalanceServiceManager();
                this.responseMessage = this.openingBalanceServiceManager.getOpeningBalanceByAccountID(requestMessage);
                log.info("account module -> api/openingBalance/getByAccountID executed");
                this.openingBalanceServiceManager=null;
                break;

            case "api/openingBalance/getByBusinessID":
                this.openingBalanceServiceManager = new OpeningBalanceServiceManager();
                this.responseMessage = this.openingBalanceServiceManager.getOpeningBalanceByBusinessID(requestMessage);
                log.info("account module -> api/openingBalance/getByBusinessID executed");
                this.openingBalanceServiceManager=null;
                break;

            case "api/openingBalance/delete":
                this.openingBalanceServiceManager = new OpeningBalanceServiceManager();
                this.responseMessage = this.openingBalanceServiceManager.delete(requestMessage);
                log.info("account module -> api/openingBalance/delete executed");
                this.openingBalanceServiceManager=null;
                break;

            default:
                System.out.println("account module -> INVALID REQUEST");
        }
    }

    //TODO: implement security check
    private boolean checkSecurity() {
        return true;
    }

}
