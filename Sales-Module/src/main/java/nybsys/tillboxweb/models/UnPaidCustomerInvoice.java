package nybsys.tillboxweb.models;

import java.util.Date;

public class UnPaidCustomerInvoice {

    public Integer customerReceiveID;
    public Integer customerID;
    public Integer customerInvoiceID;
    public String customerName;
    public Boolean allocate;
    public String documentNumber;
    public String reference;
    public Date date;
    public Double total;
    public Double amountDue;
    public Double amountReceived;
    public Double discount;

  /*  public Double unAllocate;
    public Boolean paymentStatus;
    public Double adjustAmount;*/


}
