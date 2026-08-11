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
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.LayoutInflater;
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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.canhub.cropper.CropImageView;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.factories.LanguageFactory;
import com.dsource.idc.jellowintl.activities.BaseActivity;
import com.dsource.idc.jellowintl.activities.SpeechEngineBaseActivity;
import com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardSearchActivity;
import com.dsource.idc.jellowintl.make_my_board_module.datamodels.ListItem;
import com.dsource.idc.jellowintl.make_my_board_module.datamodels.BoardIconModel;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.data_models.BoardModel;
import com.dsource.idc.jellowintl.make_my_board_module.expandable_recycler_view.SimpleListAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.OnPhotoResultCallBack;
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

public class DialogAddBoard extends DialogFragment implements IAddBoardDialogView, View.OnClickListener, View.OnFocusChangeListener {

    public interface AddBoardCallback {
        void onBoardSaved(BoardModel board);
    }

    private AddBoardCallback callback;
    private IAddBoardDialogPresenter mPresenter;
    private Context mContext;
    private OnPhotoResultCallBack reverseInterface;
    private boolean iconImageSelected = false;
    private String selectedLibraryFileName = null;
    private ListView listView;
    private CropImageView cropImageView;
    private View rootView;
    private Uri cameraUri;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && rootView != null) {
                    View cameraCropParent = rootView.findViewById(R.id.cameraCropParent);
                    cameraCropParent.setVisibility(View.VISIBLE);
                    View cropContainer = rootView.findViewById(R.id.cropContainer);
                    cropImageView.clearImage();
                    cropContainer.setVisibility(View.VISIBLE);
                    cropImageView.setImageUriAsync(uri);
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraUri != null && rootView != null) {
                    View cameraCropParent = rootView.findViewById(R.id.cameraCropParent);
                    cameraCropParent.setVisibility(View.VISIBLE);
                    View cropContainer = rootView.findViewById(R.id.cropContainer);
                    cropImageView.clearImage();
                    cropContainer.setVisibility(View.VISIBLE);
                    cropImageView.setImageUriAsync(cameraUri);
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showImageSourceDialog();
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public static DialogAddBoard newInstance(Bundle args, AddBoardCallback callback) {
        DialogAddBoard fragment = new DialogAddBoard();
        if (args != null) {
            fragment.setArguments(args);
        }
        fragment.callback = callback;
        return fragment;
    }

    public void setCallback(AddBoardCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Translucent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_add_board, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = requireContext();
        BaseActivity baseAct = (BaseActivity) requireActivity();
        mPresenter = new AddBoardDialogModel(baseAct.getAppDatabase());
        mPresenter.attachView(this);
        setupCropperTitleBar(view);

        String id = getArguments() != null ? getArguments().getString(BOARD_ID) : null;
        if (!TextUtils.isEmpty(id)) {
            mPresenter.getBoardModel(id);
        } else {
            setUpAddBoardDialog(null, view);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof BaseActivity) {
            SessionManager session = ((BaseActivity) getActivity()).getSession();
            if (!isAnalyticsActive()) {
                resetAnalytics(requireContext(), session.getUserId());
            }
        }
        startMeasuring();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof BaseActivity) {
            SessionManager session = ((BaseActivity) getActivity()).getSession();
            long sessionTime = validatePushId(session.getSessionCreatedAt());
            session.setSessionCreatedAt(sessionTime);
        }
        stopMeasuring(DialogAddBoard.class.getSimpleName());
    }

    @SuppressLint("ResourceType")
    private void setUpAddBoardDialog(final BoardModel board, View view) {
        final ImageView boardIcon = view.findViewById(R.id.board_icon);
        final Button saveButton = view.findViewById(R.id.save_button);
        final Button cancel = view.findViewById(R.id.cancel_button);
        final ImageView imageChange = view.findViewById(R.id.edit_image);
        final EditText boardName = view.findViewById(R.id.board_name);
        final Spinner languageSelect = view.findViewById(R.id.langSelectSpinner);
        final Spinner voiceSelect = view.findViewById(R.id.voiceSelectSpinner);

        view.findViewById(R.id.parent).setOnClickListener(this);
        view.findViewById(R.id.touch_inside).setOnClickListener(this);
        boardName.setOnFocusChangeListener(this);
        boardIcon.setOnClickListener(this);

        boardName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(60)});
        listView = view.findViewById(R.id.camera_list);
        int voiceSelectPos = 0;
        {
            final ArrayList<String> languageList = new ArrayList<>(Arrays.asList(LanguageFactory.getAvailableLanguages()));
            ArrayAdapter<String> langAdapter = new ArrayAdapter<>(requireContext(),
                    R.layout.simple_spinner_item, languageList);
            langAdapter.setDropDownViewResource(R.layout.popup_menu_item);
            languageSelect.setAdapter(langAdapter);
            for (String lang : SessionManager.NoTTSLang)
                languageList.remove(SessionManager.LangValueMap.get(lang));
            String voices = "";
            if (board != null) {
                voices = SpeechEngineBaseActivity.getAvailableVoicesForLanguage(board.getLanguage());
            } else {
                voices = SpeechEngineBaseActivity.getAvailableVoicesForLanguage(
                        SessionManager.LangMap.get(languageList.get(0)));
            }
            ArrayList<String> voiceList = new ArrayList<>(voices.split(",").length);
            for (int i = 0; i < voices.split(",").length; i++) {
                voiceList.add(i, "Voice " + getRomanNumber(i + 1) + getGender(voices.split(",")[i]));
            }
            if (board != null) {
                String selectVoice = board.getBoardVoice().split(",")[1].trim();
                for (int i = 0; i < voiceList.size(); i++) {
                    if (voiceList.get(i).contains(selectVoice)) {
                        voiceSelectPos = i;
                        break;
                    }
                }
            }
            ArrayAdapter<String> voiceAdapter = new ArrayAdapter<>(requireContext(),
                    R.layout.simple_spinner_item, voiceList);
            voiceAdapter.setDropDownViewResource(R.layout.popup_menu_item);
            voiceSelect.setAdapter(voiceAdapter);
            voiceSelect.setSelection(voiceSelectPos);
            if (board != null) {
                int pos = languageList.indexOf(SessionManager.LangValueMap.get(board.getLanguage()));
                languageSelect.setSelection(pos);
            }
            languageSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                    String voices = SpeechEngineBaseActivity.getAvailableVoicesForLanguage(
                            SessionManager.LangMap.get(languageList.get(position)));
                    ArrayList<String> voiceList = new ArrayList<>(voices.split(",").length);
                    for (int i = 0; i < voices.split(",").length; i++) {
                        voiceList.add(i, "Voice " + getRomanNumber(i + 1) + getGender(voices.split(",")[i]));
                    }
                    if (getContext() != null) {
                        ArrayAdapter<String> voiceAdapter = new ArrayAdapter<>(requireContext(),
                                R.layout.simple_spinner_item, voiceList);
                        voiceAdapter.setDropDownViewResource(R.layout.popup_menu_item);
                        voiceSelect.setAdapter(voiceAdapter);
                    }
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
            TextView tvLanguage = view.findViewById(R.id.tv_language);
            tvLanguage.setVisibility(View.VISIBLE);
            tvLanguage.setText(SessionManager.LangValueMap.get(board.getLanguage()));
            saveButton.setText(getString(R.string.txtSave));
        }

        saveButton.setOnClickListener(v -> {
            if (boardName.getText().toString().trim().equals("")) {
                Toast.makeText(mContext, getResources().getString(R.string.please_enter_name), Toast.LENGTH_LONG).show();
                return;
            }

            if (!iconImageSelected) {
                Toast.makeText(mContext, getResources().getString(R.string.please_select_icon), Toast.LENGTH_LONG).show();
                return;
            }

            String langCode = languageSelect.getSelectedItem().toString();
            String voiceList = SpeechEngineBaseActivity.
                    getAvailableVoicesForLanguage(SessionManager.LangMap.get(langCode));
            String voice = voiceList.split(",")[voiceSelect.getSelectedItemPosition()] + ","
                    + voiceSelect.getSelectedItem().toString().trim();

            Bitmap croppedBitmap = cropImageView.getCroppedImage();

            if (croppedBitmap == null && selectedLibraryFileName != null) {
                Glide.with(mContext)
                        .asBitmap()
                        .load(getIconPath(mContext, selectedLibraryFileName + EXTENSION))
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                                if (board == null)
                                    saveNewBoard(boardName.getText().toString().trim(), bitmap, langCode, voice);
                                else {
                                    board.setBoardVoice(voice);
                                    updateBoardDetails(board, boardName.getText().toString().trim(), bitmap);
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {}
                        });
                return;
            }

            if (croppedBitmap == null) {
                if (board == null) {
                    Toast.makeText(mContext, getString(R.string.please_crop_image_properly), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (board == null)
                saveNewBoard(boardName.getText().toString().trim(), croppedBitmap, langCode, voice);
            else {
                board.setBoardVoice(voice);
                updateBoardDetails(board, boardName.getText().toString().trim(), croppedBitmap);
            }
        });
        cancel.setOnClickListener(v -> dismiss());

        imageChange.setOnClickListener(v -> {
            if (listView.getVisibility() == View.VISIBLE)
                listView.setVisibility(View.INVISIBLE);
            else {
                listView.setVisibility(View.VISIBLE);
                listView.requestFocus();
            }
        });

        listView.setVisibility(View.INVISIBLE);
        if (board != null) {
            boardName.setText(board.getBoardName());
        }

        final ArrayList<ListItem> list = new ArrayList<>();
        @SuppressLint("Recycle") TypedArray mArray = getResources().obtainTypedArray(R.array.add_photo_option);
        list.add(new ListItem(getResources().getString(R.string.photos), mArray.getDrawable(0)));
        list.add(new ListItem(getResources().getString(R.string.library), mArray.getDrawable(1)));
        SimpleListAdapter adapter = new SimpleListAdapter(requireContext(), list);
        listView.setAdapter(adapter);
        reverseInterface = (bitmap, code, fileName) -> {
            if (code != LIBRARY_REQUEST) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Glide.with(mContext).load(stream.toByteArray()).
                        placeholder(R.drawable.ic_board_person).
                        error(R.drawable.ic_board_person).skipMemoryCache(true).
                        diskCacheStrategy(DiskCacheStrategy.NONE).
                        apply(RequestOptions.circleCropTransform()).
                        into(boardIcon);
                selectedLibraryFileName = null;
            } else {
                selectedLibraryFileName = fileName;
                Glide.with(mContext).load(getIconPath(mContext, fileName + EXTENSION))
                        .into(boardIcon);
            }
        };
        listView.setOnItemClickListener((parent, view12, position, id) -> {
            listView.setVisibility(View.INVISIBLE);
            firePhotoIntent(position);
        });

        View.OnTouchListener spinnerOnTouch = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                onClick(null);
            }
            return false;
        };
        View.OnKeyListener spinnerOnKey = (v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                onClick(null);
                return true;
            }
            return false;
        };

        languageSelect.setOnTouchListener(spinnerOnTouch);
        languageSelect.setOnKeyListener(spinnerOnKey);
        createImageCropper(view);
    }

    private void firePhotoIntent(int position) {
        if (position == 0) {
            if (hasCameraHardware()) {
                if (checkPermissionForCamera()) {
                    showImageSourceDialog();
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.camera_missing), Toast.LENGTH_LONG).show();
            }
        } else if (position == 1) {
            Bundle args = new Bundle();
            args.putString(BoardSearchActivity.SEARCH_MODE, BoardSearchActivity.BASE_ICON_SEARCH);
            BoardSearchActivity searchDialog = BoardSearchActivity.newInstance(args, (icon, resultString) -> {
                if (resultString != null) {
                    reverseInterface.onPhotoResult(null, LIBRARY_REQUEST, resultString);
                    iconImageSelected = true;
                }
            });
            searchDialog.show(getParentFragmentManager(), BoardSearchActivity.class.getSimpleName());
        }
    }

    private boolean hasCameraHardware() {
        return requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    private boolean checkPermissionForCamera() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void updateBoardDetails(BoardModel board, String name, Bitmap boardIcon) {
        if (board != null) {
            if (!name.equals(""))
                board.setBoardName(name);
            if (iconImageSelected && boardIcon != null)
                storeImageToStorage(boardIcon, board.getBoardId(), requireContext(), false);
            mPresenter.updateBoard(board);
        }
    }

    private void saveNewBoard(String boardName, Bitmap boardIcon, String langCode, String voice) {
        String boardID = (int) Calendar.getInstance().getTime().getTime() + "";

        if (iconImageSelected)
            storeImageToStorage(boardIcon, boardID, requireContext(), false);
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
        if (rootView != null) {
            setUpAddBoardDialog(board, rootView);
        }
    }

    @Override
    public void savedSuccessfully(BoardModel boardId) {
        if (callback != null) {
            callback.onBoardSaved(boardId);
        }
        dismiss();
    }

    @Override
    public void updatedSuccessfully(BoardModel board) {
        if (callback != null) {
            callback.onBoardSaved(board);
        }
        dismiss();
    }

    @Override
    public void error(String msg) {
        Log.d(getClass().getSimpleName(), msg);
    }

    @Override
    public void onClick(View v) {
        if (listView != null && listView.getVisibility() == View.VISIBLE)
            listView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        onClick(null);
    }

    private void createImageCropper(View view) {
        cropImageView = view.findViewById(R.id.cropImageView);
        cropImageView.setOnSetImageUriCompleteListener((v, uri, error) -> {
            if (error == null) {
                iconImageSelected = true;
            } else {
                Log.e("Crop", "Image load error", error);
            }
        });

        ImageView ivCrop = view.findViewById(R.id.iv_crop_image);
        ivCrop.setOnClickListener(v -> {
            Bitmap bitmap = cropImageView.getCroppedImage();
            if (bitmap != null) {
                iconImageSelected = true;
                reverseInterface.onPhotoResult(bitmap, CAMERA_REQUEST, null);
                View cropContainer = view.findViewById(R.id.cropContainer);
                cropContainer.setVisibility(View.INVISIBLE);
                View cameraCropParent = view.findViewById(R.id.cameraCropParent);
                cameraCropParent.setVisibility(View.INVISIBLE);
            } else {
                Toast.makeText(requireContext(), R.string.please_select_and_adjust_image_first, Toast.LENGTH_SHORT).show();
            }
        });

        ImageView ivBack = view.findViewById(R.id.iv_action_bar_back);
        ivBack.setOnClickListener(v -> {
            View cropContainer = view.findViewById(R.id.cropContainer);
            cropContainer.setVisibility(View.INVISIBLE);
            View cameraCropParent = view.findViewById(R.id.cameraCropParent);
            cameraCropParent.setVisibility(View.INVISIBLE);
            cropImageView.clearImage();
        });
    }

    private void showImageSourceDialog() {
        Context context = new ContextThemeWrapper(requireContext(), R.style.AppTheme);
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

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void openCamera() {
        cameraUri = createImageUri();
        if (cameraUri != null) {
            cameraLauncher.launch(cameraUri);
        }
    }

    private Uri createImageUri() {
        File file = new File(requireContext().getCacheDir(), "camera_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                file
        );
    }

    public void setupCropperTitleBar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.topBar);
        if (toolbar == null)
            return;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int height = 62;
        int startPadding = 32;
        if (getActivity() instanceof BaseActivity && ((BaseActivity) getActivity()).getScreenSize() == GlobalConstants.SCREEN_SIZE_PHONE) {
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

    private String getRomanNumber(int num) {
        if (getActivity() instanceof BaseActivity) {
            return ((BaseActivity) getActivity()).getRomanNumber(num);
        }
        return String.valueOf(num);
    }

    private String getGender(String voice) {
        if (getActivity() instanceof BaseActivity) {
            return ((BaseActivity) getActivity()).getGender(voice);
        }
        return SpeechEngineBaseActivity.voiceGender != null ? SpeechEngineBaseActivity.voiceGender.get(voice) : "";
    }
}
