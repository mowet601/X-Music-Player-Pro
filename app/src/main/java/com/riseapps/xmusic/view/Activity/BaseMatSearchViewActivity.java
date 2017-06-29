package com.riseapps.xmusic.view.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.claudiodegio.msv.BaseMaterialSearchView;
import com.riseapps.xmusic.R;
import com.riseapps.xmusic.component.CustomAnimation;
import com.riseapps.xmusic.component.SharedPreferenceSingelton;
import com.riseapps.xmusic.view.Fragment.ScrollingFragment;

/**
 * Created by kanishk on 17/03/17.
 */

public abstract class BaseMatSearchViewActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    SharedPreferenceSingelton sharedPreferenceSingelton;
    Toolbar mToolbar;
    BaseMaterialSearchView mSearchView;
    CoordinatorLayout mCoordinator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        sharedPreferenceSingelton = new SharedPreferenceSingelton();
        if (sharedPreferenceSingelton.getSavedInt(this,"Theme")==1) {
            setTheme(R.style.AppTheme_Dark);
        }else if (sharedPreferenceSingelton.getSavedInt(this,"Theme")==2) {
            setTheme(R.style.AppTheme_Dark2);
        }
        else if (sharedPreferenceSingelton.getSavedInt(this,"Theme")==3) {
            setTheme(R.style.AppTheme_Dark3);
        }
        else if (sharedPreferenceSingelton.getSavedInt(this,"Theme")==4) {
            setTheme(R.style.AppTheme_Dark4);
        }
        else if (sharedPreferenceSingelton.getSavedInt(this,"Theme")==5) {
            setTheme(R.style.AppTheme_Dark5);
        }
        else if (sharedPreferenceSingelton.getSavedInt(this,"Theme")==6) {
            setTheme(R.style.AppTheme_Dark6);
        }
        else if (sharedPreferenceSingelton.getSavedInt(this,"Theme")==7) {
            setTheme(R.style.AppTheme_Dark7);
        }
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.main_menu);
        mToolbar.showOverflowMenu();
        mSearchView = (BaseMaterialSearchView) findViewById(R.id.sv);
        mCoordinator = (CoordinatorLayout) findViewById(R.id.drawerLayout);
        mSearchView.setMenuItem(mToolbar.getMenu().findItem(R.id.action_search));
        initCustom();
        //;
    }

    public int getLayoutId() {
        return R.layout.search_simple;
    }

    protected void initCustom() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mSearchView.setMenuItem(menu.findItem(R.id.action_search));
        return true;
    }

}