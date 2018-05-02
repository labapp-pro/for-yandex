package pro.labapp.yandex.drawer;

import android.view.MenuItem;

import java.util.List;

/**
 * Created by LabApp on 11.04.2018.
 */

public interface MenuItemCallback {

    void menuItemClicked(List<NavItem> action, MenuItem item, boolean requiresPurchase);

}
