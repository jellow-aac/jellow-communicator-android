package com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs;

import static com.dsource.idc.jellowintl.factories.IconFactory.EXTENSION;
import static com.dsource.idc.jellowintl.factories.PathFactory.getBasicCustomIconsDirectory;
import static com.dsource.idc.jellowintl.factories.PathFactory.getIconPath;
import static com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogAddVerbiage.JELLOW_ID;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.CAMERA_REQUEST;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.LIBRARY_REQUEST;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.ImageStorageHelper.storeImageToStorage;
import static com.dsource.idc.jellowintl.models.GlobalConstants.BASIC_ICON_ID;
import static com.dsource.idc.jellowintl.models.GlobalConstants.BASIC_IS_CATEGORY;
import static com.dsource.idc.jellowintl.models.GlobalConstants.ENABLE_ALPHA;
import static com.dsource.idc.jellowintl.models.GlobalConstants.ICON_POSITION;
import static com.dsource.idc.jellowintl.models.GlobalConstants.IS_HOME_CATEGORY;
import static com.dsource.idc.jellowintl.models.GlobalConstants.IS_HOME_CUSTOM_ICON;
import static com.dsource.idc.jellowintl.models.GlobalConstants.RADIO_GROUP_DISABLE_ALPHA;
import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.canhub.cropper.CropImageView;
import com.dsource.idc.jellowintl.Presentor.CustomBasicIconHelper;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.BaseActivity;
import com.dsource.idc.jellowintl.make_my_board_module.activity.BoardSearchActivity;
import com.dsource.idc.jellowintl.make_my_board_module.datamodels.ListItem;
import com.dsource.idc.jellowintl.make_my_board_module.expandable_recycler_view.SimpleListAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.AddIconCallback;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.OnPhotoResultCallBack;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.models.JellowIcon;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class DialogAddEditIcon extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {

    //Static variables to set the modes
    private Context context;
    private boolean isVisible = false;
    private TextView saveButton;
    private EditText titleText;
    private TextView cancelSaveBoard;
    private ImageView editBoardIconButton;
    private ImageView iconImage;
    private ListView listView;
    private JellowIcon thisIcon = null;
    private boolean iconImageSelected = false;
    private static AddIconCallback callback;
    private String boardId;
    private OnPhotoResultCallBack revListener;
    private boolean addIcon = true;
    private boolean isCustomizedHomeIcon=false;
    private RadioGroup radioGroup;
    private CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_edit_icon);
        applyMonochromeColor();
        setupParent();
        setupCropperTitleBar();
        isCustomizedHomeIcon = getIntent().hasExtra(IS_HOME_CUSTOM_ICON);
        boardId = getIntent().getStringExtra(BOARD_ID);
        context = this;

        initViews();
        initAddEditDialog();
        if(isCustomizedHomeIcon){
            setupRadioGroup();
        }
        /*If editing existing icon custom home icons*/
        if (getIntent().getExtras() != null && getIntent().getExtras().getSerializable(JELLOW_ID) != null) {
            JellowIcon icon = (JellowIcon) getIntent().getExtras().getSerializable(JELLOW_ID);
            if (icon != null && isCustomizedHomeIcon) {
                setAlreadyPresentIcon(icon, true);
                radioGroup.setClickable(false);
                radioGroup.setAlpha(RADIO_GROUP_DISABLE_ALPHA);
                radioGroup.getChildAt(0).setEnabled(false);
                radioGroup.getChildAt(1).setEnabled(false);
            }else if (icon != null) {
                setAlreadyPresentIcon(icon, false);
            }
        }
    }

    /**
     * This method setup the radio group on the dialog.
     * Follow are conditions:-
     * if user creating a new custom icon or editing an existing custom icon at in the
     *      home (iconLocation ="00") or
     *      inside categories such as :- Greet and feel (iconLocation ="0001"),
     *          Daily Activities (iconLocation ="0002"), Eating (iconLocation ="0003"),
     *          Fun (iconLocation ="0004"), Learning ( iconLocation= "0005"),
     *          Places (iconLocation ="0007"), Time and Weather (iconLocation ="0008")
     *      then set radio as category and disable its selection, alpha
     * else if user creating a new custom icon or editing an existing custom icon at in any
     *      level 3 (iconLocation.length =6) or People (iconLocation ="0005"),
     *      Help (iconLocation ="0009")
     *      then set radio as icon and disable its selection, alpha
     * else if user creating a new custom icon or editing an existing custom icon at level 2
     *      which parent is also custom icon
     *      then set its radio based on the icons attribute "isCategory". This case is special case,
     *      here user can either create a simple icon or category icon. If received
     *      //@param icon, is empty it means, user creating a new icon, so no radio buttons are set;
     *                   And all radio buttons are enabled.
     *
     ***/
    private void setupRadioGroup() {
        boolean isHomeIcon = getIntent().getBooleanExtra(IS_HOME_CATEGORY, false);
        int levelOneIconPosition= getIntent().getIntExtra(getString(R.string.level_one_intent_pos_tag), -1);
        int levelTwoIconPosition= getIntent().getIntExtra(getString(R.string.level_2_item_pos_tag), -1);
        String iconId= getIntent().getStringExtra(BASIC_ICON_ID) != null ? getIntent().getStringExtra(BASIC_ICON_ID) : "";
        radioGroup = findViewById(R.id.rgIconOptions);
        radioGroup.setVisibility(View.VISIBLE);

        boolean radioState=false;
        float radioAlpha=RADIO_GROUP_DISABLE_ALPHA;
        // Adding an custom icon at home screen then user can add only categories there.
        if(isHomeIcon){
            radioGroup.check(R.id.rbIsCategory);
        // Adding an custom icon inside People or Help category then user can add only icons there.
        }else if (levelOneIconPosition == 5 || levelOneIconPosition == 8){
            radioGroup.check(R.id.rbIsIcon);
        // Adding an custom icon anywhere at level 3 then user can add only icons there.
        }else if(levelOneIconPosition < 9 && levelTwoIconPosition != -1){
            radioGroup.check(R.id.rbIsIcon);
        // Adding an custom icon anywhere inside level 2 apart from People and Help categories
        // then user can add only categories there.
        }else if (levelOneIconPosition < 9) {
            radioGroup.check(R.id.rbIsCategory);
        // Adding an custom icon at level 3 of any custom subcategory then user can add only icons there.
        }else if(levelOneIconPosition > 8 && levelTwoIconPosition != -1){
            radioGroup.check(R.id.rbIsIcon);
        // Adding an custom icon anywhere inside level 2 inside home screen custom icon
        // then user can add categories as well as icons there.
        }else{
            radioState = true;
            radioAlpha = ENABLE_ALPHA;
            if(CustomBasicIconHelper.givenCustomIconIsCategory(getAppDatabase(), iconId))
                radioGroup.check(R.id.rbIsCategory);
            else
                radioGroup.check(R.id.rbIsIcon);
        }
        radioGroup.setClickable(radioState);
        radioGroup.setAlpha(radioAlpha);
        radioGroup.getChildAt(0).setEnabled(radioState);
        radioGroup.getChildAt(1).setEnabled(radioState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isAnalyticsActive()){
            resetAnalytics(this, getSession().getUserId());
        }
        // Start measuring user app screen timer.
        startMeasuring();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Check if pushId is older than 24 hours (86400000 millisecond).
        // If yes then create new pushId (user session)
        // If no then do not create new pushId instead user existing and
        // current session time is saved.
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);

        // Stop measuring user app screen timer.
        stopMeasuring(DialogAddEditIcon.class.getSimpleName());
    }

    public static void subscribe(AddIconCallback addIconCallback) {
        callback = addIconCallback;
    }

    public void setAlreadyPresentIcon(JellowIcon Icon, boolean isCustomizedHomeIcon) {
        this.thisIcon = Icon;
        this.addIcon = false;
        setIconImage(isCustomizedHomeIcon);
        setTitleText(thisIcon.getIconTitle());
    }

    @SuppressLint("ResourceType")
    public void initAddEditDialog() {
        titleText.setOnFocusChangeListener(this);
        titleText.setHint(context.getResources().getString(R.string.icon_name));
        titleText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        iconImage.setOnClickListener(this);

        //List on the dialog.
        listView.setVisibility(View.INVISIBLE);
        //The list that will be shown with camera options
        final ArrayList<ListItem> list = new ArrayList<>();
        @SuppressLint("Recycle") TypedArray mArray = context.getResources().obtainTypedArray(R.array.add_photo_option);
        list.add(new ListItem(context.getResources().getString(R.string.photos), mArray.getDrawable(0)));
        list.add(new ListItem(context.getResources().getString(R.string.library), mArray.getDrawable(1)));
        SimpleListAdapter adapter = new SimpleListAdapter(context, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listView.setVisibility(View.INVISIBLE);
                firePhotoIntent(position);
            }
        });

        revListener = (bitmap, code, fileName) -> {
            if (code != LIBRARY_REQUEST) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Glide.with(context)
                        .asBitmap()
                        .load(stream.toByteArray())
                        .placeholder(R.drawable.ic_board_person)
                        .apply(RequestOptions
                                .circleCropTransform()).into(iconImage);
            } else {
                Glide.with(context).load(getIconPath(context, fileName + EXTENSION))
                        .into(iconImage);
            }
            iconImage.setBackground(context.getResources().getDrawable(R.drawable.icon_back_grey));
        };
        createImageCropper();
    }

    public void setTitleText(String name) {
        if (titleText != null) titleText.setText(name);
    }

    private void initViews() {
        //Views related to the Dialogs
        titleText = findViewById(R.id.board_name);
        saveButton = findViewById(R.id.save_board);
        cancelSaveBoard = findViewById(R.id.cancel_save_board);
        editBoardIconButton = findViewById(R.id.edit_board);
        iconImage = findViewById(R.id.board_icon);
        listView = findViewById(R.id.camera_list);
        findViewById(R.id.parent).setOnClickListener(this);
        findViewById(R.id.icon_container).setOnClickListener(this);
        iconImage.setOnClickListener(this);

        //Setting the image icon
        /*if (thisIcon != null)
            setIconImage(isCustomizedHomeIcon);*/

        //Click Listeners
        saveButton.setOnClickListener(this);
        editBoardIconButton.setOnClickListener(this);
        cancelSaveBoard.setOnClickListener(this);
        findViewById(R.id.parent).setOnClickListener(this);
        findViewById(R.id.touch_inside).setOnClickListener(this);
    }

    /**
     * This funtion takes name and bitmap array of a icon to be added and generates
     * an icon for it and adds it to the position and scrolls to it.
     *
     * @param name   Name of the Icon
     * @param bitmap bitmap array holding the image
     */
    private void addNewIcon(int id, String name, Bitmap bitmap) {
        JellowIcon icon = new JellowIcon(name, "" + id, -1, -1, id);
        icon.setVerbiageId(id + "");
        if (iconImageSelected)
            storeImageToStorage(bitmap, id + "", this, isCustomizedHomeIcon);
        if (callback != null)
            callback.onAddedSuccessfully(icon);
    }

    private void saveEditedIcon(String id, String name, Bitmap bitmapArray) {
        JellowIcon icon = new JellowIcon(name, id, -1, -1, Integer.parseInt(id));
        icon.setVerbiageId(id);
        storeImageToStorage(bitmapArray, id + "", this, isCustomizedHomeIcon);
        if (callback != null)
            callback.onAddedSuccessfully(icon);
        callback = null;
    }

    /**
     * Sets image for the Dialog
     * @param isCustomizedHomeIcon
     */
    private void setIconImage(boolean isCustomizedHomeIcon) {
        if(isCustomizedHomeIcon) {
            File en_dir = getBasicCustomIconsDirectory(this);
            String path = en_dir.getAbsolutePath();
            Glide.with(context)
                    .load(path + "/" + thisIcon.getIconDrawable() + EXTENSION)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .dontAnimate()
                    .placeholder(R.drawable.ic_board_person)
                    .into(iconImage);
        //Is a custom Icon
        }else if (thisIcon.isCustomIcon()){
            File en_dir = context.getDir(SessionManager.BOARD_ICON_LOCATION, Context.MODE_PRIVATE);
            String path = en_dir.getAbsolutePath();
            Glide.with(context)
                    .load(path + "/" + thisIcon.getIconDrawable() + EXTENSION)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .dontAnimate()
                    .placeholder(R.drawable.ic_board_person)
                    .into(iconImage);
        } else {
            Glide.with(context).load(getIconPath(context, thisIcon.getIconDrawable() + EXTENSION))
                    .skipMemoryCache(true)
                    .into(iconImage);
        }
        iconImageSelected = true;
    }


    @Override
    public void onClick(View v) {

        if (listView.getVisibility() == View.VISIBLE) listView.setVisibility(View.INVISIBLE);

        editBoardIconButton.bringToFront();

        if (v == null) return;

        if (v == editBoardIconButton && !isCustomizedHomeIcon) {
            if (isVisible)
                listView.setVisibility(View.INVISIBLE);
            else
                listView.setVisibility(View.VISIBLE);
            isVisible = !isVisible;
        }else if (v == editBoardIconButton && isCustomizedHomeIcon) {
            firePhotoIntent(0);
        } else if (v == saveButton)
            initSave();
        else if (v == cancelSaveBoard) {
            finish();

        }

    }

    private void initSave() {
        if(!iconImageSelected) {
            Toast.makeText(context,getString(R.string.please_select_icon),Toast.LENGTH_SHORT).show();
            return;
        }

        if (titleText.getText().toString().equals("")) {
            Toast.makeText(context, context.getResources().getString(R.string.please_enter_name), Toast.LENGTH_SHORT).show();
            return;
        }
        final int id = (int) Calendar.getInstance().getTimeInMillis();
        String FETCH_ENABLED;
        String IS_PRIMARY;
        Intent intent = new Intent(context, DialogAddVerbiage.class);
        Bundle bundle = new Bundle();
        if(isCustomizedHomeIcon) {
            intent.putExtra(ICON_POSITION, getIconPosition());
            boolean isCategory = (radioGroup.getCheckedRadioButtonId() == R.id.rbIsCategory);
            intent.putExtra(BASIC_IS_CATEGORY, isCategory);
            intent.putExtra(IS_HOME_CUSTOM_ICON, getIntent().hasExtra(IS_HOME_CUSTOM_ICON));
        }else
            intent.putExtra(BOARD_ID, boardId);

        final Bitmap bitmap = ((BitmapDrawable) iconImage.getDrawable()).getBitmap();

        Bitmap croppedBitmap = cropImageView.getCroppedImage();
        if (croppedBitmap == null) {
            Toast.makeText(context, getString(R.string.please_crop_image_properly), Toast.LENGTH_SHORT).show();
            return;
        }
        final String name = titleText.getText().toString();
        if (addIcon) {
            FETCH_ENABLED = "NULL";
            IS_PRIMARY = "NULL";
            bundle.putSerializable(JELLOW_ID, new JellowIcon(name, String.valueOf(id), -1, -1, id));
        } else {
            if(thisIcon.isCustomIcon() && isCustomizedHomeIcon){
                FETCH_ENABLED = thisIcon.getVerbiageId();
                IS_PRIMARY = "NULL";
                thisIcon.setIconTitle(name);
            }else if (thisIcon.isCustomIcon()) {
                //Fetch flag is set for custom icon update.
                FETCH_ENABLED = thisIcon.getVerbiageId();
                IS_PRIMARY = "NULL";
                thisIcon.setIconTitle(name);
            } else {
                //Both flags are set for primary icon to be edited
                FETCH_ENABLED = thisIcon.getVerbiageId();
                IS_PRIMARY = "TRUE";
                thisIcon.setVerbiageId(id + "");
                thisIcon.setDrawable(id + "");
                thisIcon.setIconTitle(name);
            }
            bundle.putSerializable(JELLOW_ID, thisIcon);
        }

        //Both flags are unset for new icons or categories
        intent.putExtra(DialogAddVerbiage.FETCH_FLAG, FETCH_ENABLED);
        intent.putExtra(DialogAddVerbiage.IS_PRIMARY_FLAG, IS_PRIMARY);

        intent.putExtras(bundle);
        startActivity(intent);
        finish();

        DialogAddVerbiage.callback = new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (addIcon)
                    addNewIcon(id, name, bitmap);
                else
                    saveEditedIcon(thisIcon.getVerbiageId(), name, bitmap);
            }
        };
    }

    private String getIconPosition() {
        boolean isHomeIcon = getIntent().getBooleanExtra(IS_HOME_CATEGORY, false);
        int levelOneIconPosition= getIntent().getIntExtra(getString(R.string.level_one_intent_pos_tag), -1);
        int levelTwoIconPosition= getIntent().getIntExtra(getString(R.string.level_2_item_pos_tag), -1);
        if(isHomeIcon)
            return  "00";
        else if(levelOneIconPosition != -1 && levelTwoIconPosition == -1)
            return  "00,"+ (levelOneIconPosition < 10 ? "0"+levelOneIconPosition : levelOneIconPosition);
        else if(levelOneIconPosition != -1)
            return  "00," +
                    (levelOneIconPosition < 10 ? "0"+levelOneIconPosition : levelOneIconPosition) +","+
                    (levelTwoIconPosition < 10 ? "0"+levelTwoIconPosition : levelTwoIconPosition);
        else
            return "";
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        onClick(null);
    }

    private void firePhotoIntent(int position) {
        if (position == 0) {
            //Check if the device has a camera hardware
            if(hasCameraHardware()) {
                if(checkPermissionForCamera()){
                    showImageSourceDialog();
                }else{
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                }
            }else{
                Toast.makeText(this, getResources().getString(R.string.camera_missing),Toast.LENGTH_LONG).show();
            }

        } else if (position == 1) {
            Intent intent = new Intent(this, BoardSearchActivity.class);
            intent.putExtra(BoardSearchActivity.SEARCH_MODE, BoardSearchActivity.ICON_SEARCH);
            intent.putExtra(BOARD_ID, boardId);
            startActivityForResult(intent, LIBRARY_REQUEST);
        }
    }

    public boolean checkPermissionForCamera() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result @grantResults arrays are empty.
        if (requestCode == CAMERA_REQUEST && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Bitmap bitmap = cropImageView.getCroppedImage();
                if (bitmap != null) {
                    iconImageSelected = true;
                    revListener.onPhotoResult(bitmap, CAMERA_REQUEST, null);
                } else {
                    Log.e("Crop", "Bitmap null");
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LIBRARY_REQUEST) {
            if (resultCode == RESULT_OK) {
                String fileName = data.getStringExtra("result");
                if (fileName != null) {
                    iconImageSelected = true;
                    revListener.onPhotoResult(null, requestCode, fileName);
                }
            }
        }
    }

    //region image chooser
    private void createImageCropper(){
        cropImageView = findViewById(R.id.cropImageView);
        cropImageView.setOnSetImageUriCompleteListener((view, uri, error) -> {
            if (error == null) {
                // Image is ready NOW
                iconImageSelected = true;
            } else {
                Log.e("Crop", "Image load error", error);
            }
        });

        ImageView ivCrop = findViewById(R.id.iv_crop_image);
        ivCrop.setOnClickListener(v -> {
            Bitmap bitmap = cropImageView.getCroppedImage();
            if (bitmap != null) {
                iconImageSelected = true;
                revListener.onPhotoResult(bitmap, CAMERA_REQUEST, null);
                View cropContainer = findViewById(R.id.cropContainer);
                cropContainer.setVisibility(View.INVISIBLE);
                View cameraCropParent = findViewById(R.id.cameraCropParent);
                cameraCropParent.setVisibility(View.INVISIBLE);

            } else {
                Toast.makeText(this, R.string.please_select_and_adjust_image_first, Toast.LENGTH_SHORT).show();
            }
        });

        ImageView ivBack = findViewById(R.id.iv_action_bar_back);
        ivBack.setOnClickListener(v -> {
            View cropContainer = findViewById(R.id.cropContainer);
            cropContainer.setVisibility(View.INVISIBLE);
            View cameraCropParent = findViewById(R.id.cameraCropParent);
            cameraCropParent.setVisibility(View.INVISIBLE);
            cropImageView.clearImage();
        });
    }

    private void showImageSourceDialog() {
        Context context = new ContextThemeWrapper(this, R.style.AppTheme);
        final DialogCustom dialog = new DialogCustom(context);
        dialog.setText(context.getString(R.string.select_image_source));
        dialog.setPositiveText(context.getString(R.string.camera));
        dialog.setNegativeText(context.getString(R.string.gallery));
        dialog.setOnNegativeClickListener(() -> {
            openGallery();
            dialog.dismiss();
        });
        dialog.setOnPositiveClickListener(() -> {
            openCamera();
            dialog.dismiss();
        });
        dialog.show();
    }

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    View cameraCropParent = findViewById(R.id.cameraCropParent);
                    cameraCropParent.setVisibility(View.VISIBLE);
                    View cropContainer = findViewById(R.id.cropContainer);
                    // ✅ Reset previous state
                    cropImageView.clearImage();
                    // ✅ Show crop UI
                    cropContainer.setVisibility(View.VISIBLE);
                    // ✅ Load new image
                    cropImageView.setImageUriAsync(uri);
                }
            });

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private Uri cameraUri;

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraUri != null) {
                    View cameraCropParent = findViewById(R.id.cameraCropParent);
                    cameraCropParent.setVisibility(View.VISIBLE);
                    View cropContainer = findViewById(R.id.cropContainer);
                    // ✅ Reset
                    cropImageView.clearImage();
                    // ✅ Show
                    cropContainer.setVisibility(View.VISIBLE);
                    // ✅ Load
                    cropImageView.setImageUriAsync(cameraUri);
                }
            });

    private void openCamera() {
        cameraUri = createImageUri();
        cameraLauncher.launch(cameraUri);
    }

    private Uri createImageUri() {
        File file = new File(getCacheDir(), "camera_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );
    }

    public void setupCropperTitleBar(){
        MaterialToolbar toolbar = findViewById(R.id.topBar);
        if (toolbar == null)
            return;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int height = 62;
        int startPadding = 32;
        if (getScreenSize() == GlobalConstants.SCREEN_SIZE_PHONE) {
            height = 40;
            startPadding = 24;
        }

        int heightInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                height,
                displayMetrics
        );
        ViewGroup.LayoutParams toolbarParams = toolbar.getLayoutParams();
        toolbarParams.height = heightInPx;

        int StartPaddingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                startPadding,
                displayMetrics
        );
        toolbar.setPadding(
                StartPaddingInPx,
                toolbar.getPaddingTop(),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom()
        );
        toolbar.setLayoutParams(toolbarParams);

    }
    //endregion
}
