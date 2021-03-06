package mono.hg;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import mono.hg.fragments.PreferenceFragment;
import mono.hg.helpers.PreferenceHelper;
import mono.hg.utils.ActivityServiceUtils;
import mono.hg.utils.Utils;
import mono.hg.utils.ViewUtils;
import mono.hg.wrappers.BackHandledFragment;

public class SettingsActivity extends AppCompatActivity
        implements BackHandledFragment.BackHandlerInterface {

    private BackHandledFragment selectedFragment;
    private FragmentManager fragmentManager = getSupportFragmentManager();

    @Override public void onCreate(Bundle savedInstanceState) {
        if (!PreferenceHelper.hasEditor()) {
            PreferenceHelper.initPreference(this);
        }
        PreferenceHelper.fetchPreference();

        if (PreferenceHelper.getProviderList().isEmpty()) {
            Utils.setDefaultProviders(getResources());
        }

        // Check the caller of this activity.
        // If it's coming from the launcher itself, it will always have a calling activity.
        checkCaller();

        // Load the appropriate theme.
        switch (PreferenceHelper.appTheme()) {
            default:
            case "light":
                setTheme(R.style.AppTheme);
                break;
            case "dark":
                setTheme(R.style.AppTheme_Dark);
                break;
            case "black":
                setTheme(R.style.AppTheme_Black);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fragmentManager.popBackStack("fragment_root", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        ViewUtils.setFragment(this, new PreferenceFragment());
    }

    @Override public void onBackPressed() {
        if (selectedFragment == null || !selectedFragment.onBackPressed()) {
            // Selected fragment did not consume the back press event.
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_activity_settings);
            }
            super.onBackPressed();
        }
    }

    @Override public void setSelectedFragment(BackHandledFragment selectedFragment) {
        this.selectedFragment = selectedFragment;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_activity_settings);
            }
            super.onBackPressed();
            ActivityServiceUtils.hideSoftKeyboard(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkCaller() {
        // If this activity is called from anywhere else but the launcher,
        // then the launcher needs to be informed of the changes made that it may not be aware of.
        if (getCallingActivity() == null && !PreferenceHelper.wasAlien()) {
            PreferenceHelper.isAlien(true);
        }
    }

    // Called when the activity needs to be restarted (i.e when a theme change occurs).
    // Allows for smooth transition between recreation.
    public void restartActivity() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityCompat.finishAfterTransition(this);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
    }
}
