package org.namelessrom.devicecontrol.modules.appmanager.permissions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

public class PermissionItemView extends LinearLayout implements View.OnClickListener {
    AppSecurityPermissions.MyPermissionGroupInfo mGroup;
    AppSecurityPermissions.MyPermissionInfo mPerm;
    AlertDialog mDialog;

    public PermissionItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
    }

    public void setPermission(AppSecurityPermissions.MyPermissionGroupInfo grp,
            AppSecurityPermissions.MyPermissionInfo perm, boolean first) {
        mGroup = grp;
        mPerm = perm;

        ImageView permGrpIcon = (ImageView) findViewById(R.id.perm_icon);
        TextView permNameView = (TextView) findViewById(R.id.perm_name);

        PackageManager pm = getContext().getPackageManager();
        Drawable icon = null;
        if (first) {
            icon = grp.loadGroupIcon(pm);
            if (icon != null) {
                icon = DrawableHelper.applyAccentColorFilter(icon.mutate());
            }
        }
        CharSequence label = perm.mLabel;

        permGrpIcon.setImageDrawable(icon);
        permNameView.setText(label);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mGroup != null && mPerm != null) {
            if (mDialog != null) {
                mDialog.dismiss();
            }
            PackageManager pm = getContext().getPackageManager();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(mGroup.mLabel);
            if (mPerm.descriptionRes != 0) {
                builder.setMessage(mPerm.loadDescription(pm));
            } else {
                CharSequence appName;
                try {
                    ApplicationInfo app = pm.getApplicationInfo(mPerm.packageName, 0);
                    appName = app.loadLabel(pm);
                } catch (PackageManager.NameNotFoundException e) {
                    appName = mPerm.packageName;
                }
                builder.setMessage(getContext().getString(R.string.perms_description_app, appName)
                        + "\n\n" + mPerm.name);
            }
            builder.setCancelable(true);
            builder.setIcon(mGroup.loadGroupIcon(pm));
            mDialog = builder.show();
            mDialog.setCanceledOnTouchOutside(true);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

}
