/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.glass.sample.compass;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.view.WindowUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

/**
 * This activity manages the options menu that appears when the user taps on the compass's live
 * card or says "ok glass" while the live card is settled.
 */
public class CompassMenuActivity extends Activity {

    private CompassService.CompassBinder mCompassService;

    private boolean mFromLiveCardVoice;

    // Requested actions.
    private boolean mDoReadAloud;
    private boolean mDoStop;
    private boolean mDoFinish;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof CompassService.CompassBinder) {
                mCompassService = (CompassService.CompassBinder) service;
                performActionsIfConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Do nothing.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFromLiveCardVoice = getIntent().getBooleanExtra(LiveCard.EXTRA_FROM_LIVECARD_VOICE, false);
        if (mFromLiveCardVoice) {
            // When activated by voice from a live card, enable voice commands. The menu
            // will automatically "jump" ahead to the items (skipping the guard phrase
            // that was already said at the live card).
            getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        }
        bindService(new Intent(this, CompassService.class), mConnection, 0);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mFromLiveCardVoice) {
            // When not activated by voice, we are activated by TAP from a live card.
            // Open the options menu as soon as window attaches.
            openOptionsMenu();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (isMyMenu(featureId)) {
            getMenuInflater().inflate(R.menu.compass, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (isMyMenu(featureId)) {
            // Don't reopen menu once we are finishing. This is necessary
            // since voice menus reopen themselves while in focus.
            return !mDoFinish;
        }
        return super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (isMyMenu(featureId)) {
            switch (item.getItemId()) {
                case R.id.read_aloud:
                    mDoReadAloud = true;
                    return true;
                case R.id.stop_this:
                    mDoStop = true;
                    return true;
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
        if (isMyMenu(featureId)) {
           // When the menu panel closes, either an item is selected from the menu or the
           // menu is dismissed by swiping down. Either way, we end the activity.
           mDoFinish = true;
           performActionsIfConnected();
        }
    }

    /*
     * Performs the requested actions if connected. Since the connection may establish
     * either before or after actions are requested, we simply record requested actions,
     * and try to perform them both when the action establishes and when menu panel
     * closes (either due to dismissing or selecting an item).
     */
    private void performActionsIfConnected() {
        if (mCompassService != null) {
            if (mDoReadAloud) {
                mDoReadAloud = false;
                mCompassService.readHeadingAloud();
            }
            if (mDoStop){
                mDoStop = false;
                stopService(new Intent(CompassMenuActivity.this, CompassService.class));
            }
            if (mDoFinish) {
                mCompassService = null;
                unbindService(mConnection);
                finish();
            }
        }
    }

    /**
     * Returns {@code true} when the {@code featureId} belongs to the options menu or voice
     * menu that are controlled by this menu activity.
     */
    private boolean isMyMenu(int featureId) {
        return featureId == Window.FEATURE_OPTIONS_PANEL ||
               featureId == WindowUtils.FEATURE_VOICE_COMMANDS;
    }
}
