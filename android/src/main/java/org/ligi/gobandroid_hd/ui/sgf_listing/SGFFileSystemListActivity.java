/**
 * gobandroid
 * by Marcus -Ligi- Bueschleb
 * http://ligi.de
 * <p/>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation;
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package org.ligi.gobandroid_hd.ui.sgf_listing;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.ligi.gobandroid_hd.InteractionScope;
import org.ligi.gobandroid_hd.R;
import org.ligi.gobandroid_hd.ui.GobandroidNotifications;
import org.ligi.gobandroid_hd.ui.application.GobandroidFragmentActivity;
import org.ligi.gobandroid_hd.ui.tsumego.fetch.DownloadProblemsDialog;
import org.ligi.gobandroid_hd.ui.tsumego.fetch.TsumegoSource;

import java.io.File;

import static org.ligi.gobandroid_hd.InteractionScope.Mode.REVIEW;
import static org.ligi.gobandroid_hd.InteractionScope.Mode.TSUMEGO;

/**
 * Activity to load SGF's from SD card
 */

public class SGFFileSystemListActivity extends GobandroidFragmentActivity {

    private SGFListFragment list_fragment;
    private AsyncTask<TsumegoSource[], String, Integer> downloadProblemsTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list);

        if (getIntent().getBooleanExtra(GobandroidNotifications.BOOL_FROM_NOTIFICATION_EXTRA_KEY, false)) {
            new GobandroidNotifications(this).cancelNewTsumegosNotification();
        }

        final String sgfPath = getSGFPath();

        if (sgfPath.substring(sgfPath.indexOf('/')).startsWith(settings.getTsumegoPath().substring(sgfPath.indexOf('/')))) {
            interactionScope.setMode(InteractionScope.Mode.TSUMEGO);
        }

        if (sgfPath.substring(sgfPath.indexOf('/')).startsWith(settings.getReviewPath().substring(sgfPath.indexOf('/')))) {
            interactionScope.setMode(REVIEW);
        }

        final File dir = new File(sgfPath);

        setActionbarProperties(dir);

        list_fragment = SGFListFragment.newInstance(dir);
        getSupportFragmentManager().beginTransaction().replace(R.id.list_fragment, list_fragment).commit();
    }

    private String getSGFPath() {
        if (getIntent().getData() != null) {
            return getIntent().getData().getPath();
        }

        return settings.getSGFBasePath();
    }

    private void setActionbarProperties(final File dir) {
        switch (interactionScope.getMode()) {
            case TSUMEGO:
                setTitle(R.string.load_tsumego);
                break;
            default:
                // we can only show stuff for tsumego and review - if in doubt -
                // trade as review
                interactionScope.setMode(REVIEW);
                // fall wanted

            case REVIEW:
                setTitle(R.string.load_game);
                break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setSubtitle(dir.getAbsolutePath());
        }
    }

    @Override
    protected void onStop() {
        if (downloadProblemsTask != null) {
            downloadProblemsTask.cancel(true);
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (interactionScope.getMode() == TSUMEGO) {
            getMenuInflater().inflate(R.menu.refresh_tsumego, menu);
        }

        if (interactionScope.getMode() == REVIEW) {
            getMenuInflater().inflate(R.menu.review_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                downloadProblemsTask = DownloadProblemsDialog.getAndRunTask(this, list_fragment);
                return true;
            case R.id.menu_del_sgfmeta:
                list_fragment.delete_sgfmeta();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
