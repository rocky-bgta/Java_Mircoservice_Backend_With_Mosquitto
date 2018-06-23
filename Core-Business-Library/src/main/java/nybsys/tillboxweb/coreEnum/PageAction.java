/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 05/04/2018
 * Time: 12:30
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.coreEnum;

public enum PageAction {

    QuickView(1),
    Edit(2),
    Delete(3);
    private int pageAction;

    PageAction(int pageAction) {
        this.pageAction = pageAction;
    }

    public int get() {
        return this.pageAction;
    }
}
