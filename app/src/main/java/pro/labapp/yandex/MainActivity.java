package pro.labapp.yandex;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pro.labapp.yandex.drawer.MenuItemCallback;
import pro.labapp.yandex.drawer.NavItem;
import pro.labapp.yandex.drawer.SimpleMenu;
import pro.labapp.yandex.drawer.TabAdapter;
import pro.labapp.yandex.inherit.BackPressFragment;
import pro.labapp.yandex.inherit.PermisFragment;
import pro.labapp.yandex.util.DisableableViewPager;
import pro.labapp.yandex.util.Helper;

public class MainActivity extends AppCompatActivity implements MenuItemCallback, Parser.CallBack {

    private Toolbar mToolbar;
    private TabLayout tabLayout;
    private DisableableViewPager viewPager;
    private NavigationView navigationView;
    private static SimpleMenu menu;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private TabAdapter adapter;

    private int interstitialCount = -1;

    public static String FRAGMENT_DATA = "transaction_data";
    public static String FRAGMENT_CLASS = "transation_target";

    public static boolean TABLET_LAYOUT = true;


    List<NavItem> queueItem;
    MenuItem queueMenuItem;


    @Override
    public void configLoaded(boolean facedException) {
        if (facedException || menu.getFirstMenuItem() == null) {
            if (Helper.isOnlineShowDialog(MainActivity.this))
                Toast.makeText(this, R.string.invalid_configuration, Toast.LENGTH_LONG).show();
        } else {

            menuItemClicked(menu.getFirstMenuItem().getValue(), menu.getFirstMenuItem().getKey(), false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (useTabletMenu()) {
            setContentView(R.layout.activity_main_tablet);
            Helper.setStatusBarColor(MainActivity.this,
                    ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            setContentView(R.layout.activity_main);
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (!useTabletMenu())
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        else {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }


        if (!useTabletMenu()) {
            drawer = (DrawerLayout) findViewById(R.id.drawer);
            toggle = new ActionBarDrawerToggle(
                    this, drawer, mToolbar, R.string.drawer_open, R.string.drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }


        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (DisableableViewPager) findViewById(R.id.viewpager);


        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(FRAGMENT_CLASS)) {
            try {
                Class<? extends Fragment> fragmentClass = (Class<? extends Fragment>) getIntent().getExtras().getSerializable(FRAGMENT_CLASS);
                if (fragmentClass != null) {
                    String[] extra = getIntent().getExtras().getStringArray(FRAGMENT_DATA);
                    HolderActivity.startActivity(this, fragmentClass, extra);
                    finish();

                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        //Menu items
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        menu = new SimpleMenu(navigationView.getMenu(), this);
        if (Config.USE_HARDCODED_CONFIG) {
            Config.configureMenu(menu, this);
        } else if (!Config.CONFIG_URL.isEmpty() && Config.CONFIG_URL.contains("http"))
            new Parser(Config.CONFIG_URL, menu, this, this).execute();
        else
            new Parser("config.json", menu, this, this).execute();
        tabLayout.setupWithViewPager(viewPager);

        if (!useTabletMenu()) {
            drawer.setStatusBarBackgroundColor(
                    ContextCompat.getColor(this, R.color.colorPrimary));
        }

        applyDrawerLocks();


    }

    @SuppressLint({"NewApi", "WrongConstant"})
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 1:
                boolean foundfalse = false;
                int res = 0;
                for (String perms : permissions) {
                    res = checkCallingOrSelfPermission(perms);
                    if (!(res == PackageManager.PERMISSION_GRANTED)) {
                        foundfalse = true;
                    }
                }
                if (!foundfalse) {

                    menuItemClicked(queueItem, queueMenuItem, false);

                } else {

                    Toast.makeText(MainActivity.this, getResources().getString(R.string.permissions_required), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void menuItemClicked(List<NavItem> actions, MenuItem item, boolean requiresPurchase) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean openOnStart = prefs.getBoolean("menuOpenOnStart", false);
        if (drawer != null)
            if (openOnStart && !useTabletMenu() && adapter == null) {
                drawer.openDrawer(GravityCompat.START);
            } else {

                drawer.closeDrawer(GravityCompat.START);
            }


        if (!checkPermissionsHandleIfNeeded(actions, item)) return;

        if (item != null) {
            for (MenuItem menuItem : menu.getMenuItems())
                menuItem.setChecked(false);
            item.setChecked(true);
        }

        adapter = new TabAdapter(getSupportFragmentManager(), actions, this);
        viewPager.setAdapter(adapter);

        if (actions.size() == 1) {
            tabLayout.setVisibility(View.GONE);
            viewPager.setPagingEnabled(false);
        } else {
            tabLayout.setVisibility(View.VISIBLE);
            viewPager.setPagingEnabled(true);
        }
    }



    private boolean checkPermissionsHandleIfNeeded(List<NavItem> tabs, MenuItem item) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) return true;

        List<String> allPermissions = new ArrayList<>();
        for (NavItem tab : tabs) {
            if (PermisFragment.class.isAssignableFrom(tab.getFragment())) {
                try {
                    allPermissions.addAll(Arrays.asList(((PermisFragment) tab.getFragment().newInstance()).requiredPermissions()));
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }

        if (allPermissions.size() > 1) {
            boolean allGranted = true;
            for (String permission : allPermissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    allGranted = false;
            }

            if (!allGranted) {

                requestPermissions(allPermissions.toArray(new String[0]), 1);
                queueItem = tabs;
                queueMenuItem = item;
                return false;
            }

            return true;
        }

        return true;
    }



    @Override
    public void onBackPressed() {
        Fragment activeFragment = null;
        if (adapter != null)
            activeFragment = adapter.getCurrentFragment();

        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (activeFragment instanceof BackPressFragment) {
            boolean handled = ((BackPressFragment) activeFragment).handleBackPress();
            if (!handled) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        @SuppressLint("RestrictedApi") List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null)
            for (Fragment frag : fragments)
                if (frag != null)
                    frag.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
    }


    public boolean useTabletMenu() {
        return (getResources().getBoolean(R.bool.isWideTablet) && TABLET_LAYOUT);
    }


    public void applyDrawerLocks() {
        if (drawer == null) {
            if (Config.HIDE_DRAWER)
                navigationView.setVisibility(View.GONE);
            return;
        }

        if (Config.HIDE_DRAWER) {
            toggle.setDrawerIndicatorEnabled(false);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

}

