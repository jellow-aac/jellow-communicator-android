package com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs;

import static com.dsource.idc.jellowintl.factories.IconFactory.EXTENSION;
import static com.dsource.idc.jellowintl.factories.PathFactory.getIconPath;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.BOARD_ID;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.CAMERA_REQUEST;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.LIBRARY_REQUEST;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.ImageStorageHelper.storeImageToStorage;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.canhub.cropper.CropImageView;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.BaseActivity;
import com.dsource.idc.jellowintl.activities.SpeechEngineBaseActivity;
import com.dsource.idc.jellowintl.factories.LanguageFactory;
import com.dsource.idc.jellowintl.make_my_board_module.activity.BoardSearchActivity;
import com.dsource.idc.jellowintl.make_my_board_module.datamodels.BoardIconModel;
import com.dsource.idc.jellowintl.make_my_board_module.datamodels.ListItem;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.expandable_recycler_view.SimpleListAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.OnPhotoResultCallBack;
import com.dsource.idc.jellowintl.make_my_board_module.managers.BoardLanguageManager;
import com.dsource.idc.jellowintl.make_my_board_module.models.AddBoardDialogModel;
import com.dsource.idc.jellowintl.make_my_board_module.presenter_interfaces.IAddBoardDialogPresenter;
import com.dsource.idc.jellowintl.make_my_board_module.view_interfaces.IAddBoardDialogView;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.models.JellowIcon;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class DialogAddBoard extends BaseActivity implements IAddBoardDialogView,View.OnClickListener, View.OnFocusChangeListener {

    private IAddBoardDialogPresenter mPresenter;
    private Context mContext;
    private OnPhotoResultCallBack reverseInterface;
    private boolean iconImageSelected = false;
    private ListView listView;
    private CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_board);
        applyMonochromeColor();
        mContext = this;
        mPresenter = new AddBoardDialogModel(getAppDatabase());
        mPresenter.attachView(this);
        setupParent();
        setupCropperTitleBar();

        //Fetch Board Id
        if (getIntent().getStringExtra(BOARD_ID) != null) {
            String id = getIntent().getStringExtra(BOARD_ID);
            if (TextUtils.isEmpty(id))
                setUpAddBoardDialog(null);
            else mPresenter.getBoardModel(id);
        } else
            setUpAddBoardDialog(null);
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
        stopMeasuring(DialogAddBoard.class.getSimpleName());
    }

    @SuppressLint("ResourceType")
    private void setUpAddBoardDialog(final BoardModel board) {
        final ImageView boardIcon = findViewById(R.id.board_icon);
        final Button saveButton = findViewById(R.id.save_button);
        final Button cancel = findViewById(R.id.cancel_button);
        final ImageView imageChange = findViewById(R.id.edit_image);
        final EditText boardName = findViewById(R.id.board_name);
        final Spinner languageSelect = findViewById(R.id.langSelectSpinner);
        final Spinner voiceSelect = findViewById(R.id.voiceSelectSpinner);

        findViewById(R.id.parent).setOnClickListener(this);
        findViewById(R.id.touch_inside).setOnClickListener(this);
        boardName.setOnFocusChangeListener(this);
        boardIcon.setOnClickListener(this);

        boardName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(60)});
        listView = findViewById(R.id.camera_list);
        int voiceSelectPos = 0;
        {
            final ArrayList<String> languageList = new ArrayList<>(Arrays.asList(LanguageFactory.getAvailableLanguages()));
            ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, languageList);
            langAdapter.setDropDownViewResource(R.layout.popup_menu_item);
            languageSelect.setAdapter(langAdapter);
            for (String lang : SessionManager.NoTTSLang)
                languageList.remove(SessionManager.LangValueMap.get(lang));
            String voices= "";
            if(board != null){
                voices = SpeechEngineBaseActivity.getAvailableVoicesForLanguage(board.getLanguage());
            }else{
                voices = SpeechEngineBaseActivity.getAvailableVoicesForLanguage(
                        SessionManager.LangMap.get(languageList.get(0)));
            }
            ArrayList<String> voiceList = new ArrayList<>(voices.split(",").length);
            for (int i = 0; i < voices.split(",").length; i++) {
                voiceList.add(i,"Voice "+ getRomanNumber(i+1)+getGender(voices.split(",")[i]));
            }
            if(board != null) {
                String selectVoice = board.getBoardVoice().split(",")[1].trim();
                for (int i = 0; i < voiceList.size(); i++) {
                    if (voiceList.get(i).contains(selectVoice)) {
                        voiceSelectPos = i;
                        break;
                    }
                }
            }
            ArrayAdapter<String> voiceAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, voiceList);
            voiceAdapter.setDropDownViewResource(R.layout.popup_menu_item);
            voiceSelect.setAdapter(voiceAdapter);
            voiceSelect.setSelection(voiceSelectPos);
            if(board != null) {
                int pos = languageList.indexOf(SessionManager.LangValueMap.get(board.getLanguage()));
                languageSelect.setSelection(pos);
            }
            languageSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String voices = SpeechEngineBaseActivity.getAvailableVoicesForLanguage(
                            SessionManager.LangMap.get(languageList.get(position)));
                    ArrayList<String> voiceList = new ArrayList<>(voices.split(",").length);
                    for (int i = 0; i < voices.split(",").length; i++) {
                        voiceList.add(i,"Voice "+ getRomanNumber(i+1)+getGender(voices.split(",")[i]));
                    }
                    ArrayAdapter<String> voiceAdapter = new ArrayAdapter<>(DialogAddBoard.this,
                            android.R.layout.simple_spinner_item, voiceList);
                    voiceAdapter.setDropDownViewResource(R.layout.popup_menu_item);
                    voiceSelect.setAdapter(voiceAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
        if (board != null) {
            boardName.setText(board.getBoardName());
            File en_dir = mContext.getDir(SessionManager.BOARD_ICON_LOCATION, Context.MODE_PRIVATE);
            String path = en_dir.getAbsolutePath();

            iconImageSelected = true;

            Glide.with(mContext)
                    .load(path + "/" + board.getBoardId() + ".png")
                    .placeholder(R.drawable.ic_board_person)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .transform(new CircleCrop())
                    .dontAnimate()
                    .into(boardIcon);
            languageSelect.setVisibility(View.GONE);
            TextView tvLanguage = findViewById(R.id.tv_language);
            tvLanguage.setVisibility(View.VISIBLE);
            tvLanguage.setText(SessionManager.LangValueMap.get(board.getLanguage()));
            saveButton.setText(getString(R.string.txtSave));
        }

        saveButton.setOnClickListener(v -> {

            if (boardName.getText().toString().trim().equals("")) {
                Toast.makeText(mContext, getResources().getString(R.string.please_enter_name), Toast.LENGTH_LONG).show();
                return;
            }

            if(!iconImageSelected){
                Toast.makeText(mContext, getResources().getString(R.string.please_select_icon), Toast.LENGTH_LONG).show();
                return;
            }

            //Returns code for each language in board
            String langCode = languageSelect.getSelectedItem().toString();
            String voiceList =  SpeechEngineBaseActivity.
                    getAvailableVoicesForLanguage(SessionManager.LangMap.get(langCode));
            String voice = voiceList.split(",")[voiceSelect.getSelectedItemPosition()]+","
                    +voiceSelect.getSelectedItem().toString().trim();
            Bitmap croppedBitmap = cropImageView.getCroppedImage();
            if (croppedBitmap == null) {
                Toast.makeText(mContext, "Please crop image properly", Toast.LENGTH_SHORT).show();
                return;
            }
            if (board == null)
                saveNewBoard(boardName.getText().toString().trim(), croppedBitmap, langCode, voice);
            else {
                board.setBoardVoice(voice);
                updateBoardDetails(board, boardName.getText().toString().trim(), croppedBitmap);
            }
            finish();
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listView.getVisibility() == View.VISIBLE)
                    listView.setVisibility(View.INVISIBLE);
                else{
                    listView.setVisibility(View.VISIBLE);
                    listView.requestFocus();
                }
            }
        });

        //List on the dialog.
        listView.setVisibility(View.INVISIBLE);
        if (board != null) {
            boardName.setText(board.getBoardName());
        }
        //The list that will be shown with camera options
        final ArrayList<ListItem> list = new ArrayList<>();
        @SuppressLint("Recycle") TypedArray mArray = getResources().obtainTypedArray(R.array.add_photo_option);
        list.add(new ListItem(getResources().getString(R.string.photos), mArray.getDrawable(0)));
        list.add(new ListItem(getResources().getString(R.string.library), mArray.getDrawable(1)));
        SimpleListAdapter adapter = new SimpleListAdapter(this, list);
        listView.setAdapter(adapter);
        reverseInterface = (bitmap, code, fileName) -> {
            if (code != LIBRARY_REQUEST) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Glide.with(mContext).load(stream.toByteArray()).
                        transform(new CircleCrop()).
                        placeholder(R.drawable.ic_board_person).
                        error(R.drawable.ic_board_person).skipMemoryCache(true).
                        diskCacheStrategy(DiskCacheStrategy.NONE).
                        into(boardIcon);
            } else {
                Glide.with(mContext).load(getIconPath(mContext, fileName + EXTENSION))
                        .into(boardIcon);
            }
        };
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listView.setVisibility(View.INVISIBLE);
                firePhotoIntent(position);
            }
        });

        View.OnTouchListener spinnerOnTouch = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onClick(null);
                }
                return false;
            }
        };
        View.OnKeyListener spinnerOnKey = new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    onClick(null);
                    return true;
                } else {
                    return false;
                }
            }
        };

        languageSelect.setOnTouchListener(spinnerOnTouch);
        languageSelect.setOnKeyListener(spinnerOnKey);
        createImageCropper();
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
            intent.putExtra(BoardSearchActivity.SEARCH_MODE, BoardSearchActivity.BASE_ICON_SEARCH);
            startActivityForResult(intent, LIBRARY_REQUEST);
        }
    }

    private boolean checkPermissionForCamera() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result @grantResults arrays are empty.
        if (requestCode == CAMERA_REQUEST && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Bitmap bitmap = cropImageView.getCroppedImage();
                if (bitmap != null) {
                    iconImageSelected = true;
                    reverseInterface.onPhotoResult(bitmap, CAMERA_REQUEST, null);
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
        if (resultCode == RESULT_OK) {
            /*
             * In this, we are collecting the name of the icon clicked on the search bar and using that to fetch the icon from the database.
             */
            if (requestCode == LIBRARY_REQUEST) {
                String fileName = data.getStringExtra("result");
                if (fileName != null) {
                    reverseInterface.onPhotoResult(null, requestCode, fileName);
                    iconImageSelected = true;
                }
            }
        }
    }

    private void updateBoardDetails(BoardModel board, String name, Bitmap boardIcon) {

        if (board != null) {
            if (!name.equals(""))
                board.setBoardName(name);
            if (iconImageSelected)
                storeImageToStorage(boardIcon, board.getBoardId(), this, false);
            mPresenter.updateBoard(board);
        }
    }

    private void saveNewBoard(String boardName, Bitmap boardIcon, String langCode, String voice) {
        String boardID = (int) Calendar.getInstance().getTime().getTime() + "";

        if (iconImageSelected)
            storeImageToStorage(boardIcon, boardID, this, false);
        BoardModel newBoard = new BoardModel();
        newBoard.setBoardName(boardName);
        newBoard.setBoardId(boardID);
        newBoard.setGridSize(4);
        newBoard.setLanguage(SessionManager.LangMap.get(langCode));
        newBoard.setBoardVoice(voice);
        newBoard.setIconModel(new BoardIconModel(new JellowIcon("", "", -1, -1, -1)));
        mPresenter.saveBoard(newBoard);
    }

    @Override
    public void boardRetrieved(BoardModel board) {
        setUpAddBoardDialog(board);
    }

    @Override
    public void savedSuccessfully(BoardModel boardId) {
        new BoardLanguageManager(boardId,mContext,getAppDatabase()).checkLanguageAvailabilityInBoard();
        finish();
    }

    @Override
    public void updatedSuccessfully(BoardModel board) {}

    @Override
    public void error(String msg) {
        Log.d(getClass().getSimpleName(), msg);
    }

    @Override
    public void onClick(View v) {
        if(listView.getVisibility()==View.VISIBLE)
            listView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        onClick(null);
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
                reverseInterface.onPhotoResult(bitmap, CAMERA_REQUEST, null);
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
