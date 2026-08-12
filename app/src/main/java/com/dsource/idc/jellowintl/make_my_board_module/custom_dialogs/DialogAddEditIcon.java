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
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.canhub.cropper.CropImageView;
import com.dsource.idc.jellowintl.Presentor.CustomBasicIconHelper;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.BaseActivity;
import com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardSearchActivity;
import com.dsource.idc.jellowintl.make_my_board_module.datamodels.ListItem;
import com.dsource.idc.jellowintl.make_my_board_module.expandable_recycler_view.SimpleListAdapter;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.AddIconCallback;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.OnPhotoResultCallBack;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants;
import com.dsource.idc.jellowintl.models.JellowIcon;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class DialogAddEditIcon extends DialogFragment implements View.OnClickListener, View.OnFocusChangeListener {

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
    private boolean isCustomizedHomeIcon = false;
    private RadioGroup radioGroup;
    private CropImageView cropImageView;
    private String selectedLibraryFileName = null;
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

    public static DialogAddEditIcon newInstance(Bundle args, AddIconCallback addIconCallback) {
        DialogAddEditIcon fragment = new DialogAddEditIcon();
        if (args != null) {
            fragment.setArguments(args);
        }
        callback = addIconCallback;
        return fragment;
    }

    public static void subscribe(AddIconCallback addIconCallback) {
        callback = addIconCallback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Translucent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_add_edit_icon, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = requireContext();
        setupCropperTitleBar(view);
        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        isCustomizedHomeIcon = args.containsKey(IS_HOME_CUSTOM_ICON);
        boardId = args.getString(BOARD_ID);

        initViews(view);
        initAddEditDialog(view);
        if (isCustomizedHomeIcon) {
            setupRadioGroup(view);
        }
        /*If editing existing icon custom home icons*/
        if (args.getSerializable(JELLOW_ID) != null) {
            JellowIcon icon = (JellowIcon) args.getSerializable(JELLOW_ID);
            if (icon != null && isCustomizedHomeIcon) {
                setAlreadyPresentIcon(icon, true);
                if (radioGroup != null) {
                    radioGroup.setClickable(false);
                    radioGroup.setAlpha(RADIO_GROUP_DISABLE_ALPHA);
                    radioGroup.getChildAt(0).setEnabled(false);
                    radioGroup.getChildAt(1).setEnabled(false);
                }
            } else if (icon != null) {
                setAlreadyPresentIcon(icon, false);
            }
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
        stopMeasuring(DialogAddEditIcon.class.getSimpleName());
    }

    private void setupRadioGroup(View view) {
        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        boolean isHomeIcon = args.getBoolean(IS_HOME_CATEGORY, false);
        int levelOneIconPosition = args.getInt(getString(R.string.level_one_intent_pos_tag), -1);
        int levelTwoIconPosition = args.getInt(getString(R.string.level_2_item_pos_tag), -1);
        String iconId = args.getString(BASIC_ICON_ID) != null ? args.getString(BASIC_ICON_ID) : "";
        radioGroup = view.findViewById(R.id.rgIconOptions);
        radioGroup.setVisibility(View.VISIBLE);

        boolean radioState = false;
        float radioAlpha = RADIO_GROUP_DISABLE_ALPHA;
        if (isHomeIcon) {
            radioGroup.check(R.id.rbIsCategory);
        } else if (levelOneIconPosition == 5 || levelOneIconPosition == 8) {
            radioGroup.check(R.id.rbIsIcon);
        } else if (levelOneIconPosition < 9 && levelTwoIconPosition != -1) {
            radioGroup.check(R.id.rbIsIcon);
        } else if (levelOneIconPosition < 9) {
            radioGroup.check(R.id.rbIsCategory);
        } else if (levelOneIconPosition > 8 && levelTwoIconPosition != -1) {
            radioGroup.check(R.id.rbIsIcon);
        } else {
            radioState = true;
            radioAlpha = ENABLE_ALPHA;
            BaseActivity baseAct = (BaseActivity) requireActivity();
            if (CustomBasicIconHelper.givenCustomIconIsCategory(baseAct.getAppDatabase(), iconId))
                radioGroup.check(R.id.rbIsCategory);
            else
                radioGroup.check(R.id.rbIsIcon);
        }
        radioGroup.setClickable(radioState);
        radioGroup.setAlpha(radioAlpha);
        radioGroup.getChildAt(0).setEnabled(radioState);
        radioGroup.getChildAt(1).setEnabled(radioState);
    }

    public void setAlreadyPresentIcon(JellowIcon Icon, boolean isCustomizedHomeIcon) {
        this.thisIcon = Icon;
        this.addIcon = false;
        setIconImage(isCustomizedHomeIcon);
        setTitleText(thisIcon.getIconTitle());
    }

    @SuppressLint("ResourceType")
    public void initAddEditDialog(View view) {
        titleText.setOnFocusChangeListener(this);
        titleText.setHint(context.getResources().getString(R.string.icon_name));
        titleText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        iconImage.setOnClickListener(this);

        listView.setVisibility(View.INVISIBLE);
        final ArrayList<ListItem> list = new ArrayList<>();
        @SuppressLint("Recycle") TypedArray mArray = context.getResources().obtainTypedArray(R.array.add_photo_option);
        list.add(new ListItem(context.getResources().getString(R.string.photos), mArray.getDrawable(0)));
        list.add(new ListItem(context.getResources().getString(R.string.library), mArray.getDrawable(1)));
        SimpleListAdapter adapter = new SimpleListAdapter(context, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            listView.setVisibility(View.INVISIBLE);
            firePhotoIntent(position);
        });

        revListener = (bitmap, code, fileName) -> {
            if (code != LIBRARY_REQUEST) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Glide.with(context)
                        .asBitmap()
                        .load(stream.toByteArray())
                        .placeholder(R.drawable.ic_board_person)
                        .apply(RequestOptions.circleCropTransform()).into(iconImage);
                selectedLibraryFileName = null;
            } else {
                Glide.with(context).load(getIconPath(context, fileName + EXTENSION))
                        .into(iconImage);
                selectedLibraryFileName = fileName;
            }
            iconImage.setBackground(context.getResources().getDrawable(R.drawable.icon_back_grey));
        };
        createImageCropper(view);
    }

    public void setTitleText(String name) {
        if (titleText != null) titleText.setText(name);
    }

    private void initViews(View view) {
        titleText = view.findViewById(R.id.board_name);
        saveButton = view.findViewById(R.id.save_board);
        cancelSaveBoard = view.findViewById(R.id.cancel_save_board);
        editBoardIconButton = view.findViewById(R.id.edit_board);
        iconImage = view.findViewById(R.id.board_icon);
        listView = view.findViewById(R.id.camera_list);
        view.findViewById(R.id.parent).setOnClickListener(this);
        view.findViewById(R.id.icon_container).setOnClickListener(this);
        iconImage.setOnClickListener(this);

        saveButton.setOnClickListener(this);
        editBoardIconButton.setOnClickListener(this);
        cancelSaveBoard.setOnClickListener(this);
        view.findViewById(R.id.parent).setOnClickListener(this);
        view.findViewById(R.id.touch_inside).setOnClickListener(this);
    }

    private void addNewIcon(int id, String name, Bitmap bitmap) {
        JellowIcon icon = new JellowIcon(name, "" + id, -1, -1, id);
        icon.setVerbiageId(id + "");
        if (iconImageSelected && bitmap != null)
            storeImageToStorage(bitmap, id + "", context, isCustomizedHomeIcon);
        if (callback != null)
            callback.onAddedSuccessfully(icon);
    }

    private void saveEditedIcon(String id, String name, Bitmap bitmapArray) {
        JellowIcon icon = new JellowIcon(name, id, -1, -1, Integer.parseInt(id));
        icon.setVerbiageId(id);
        if (iconImageSelected && bitmapArray != null) {
            storeImageToStorage(bitmapArray, id + "", context, isCustomizedHomeIcon);
        }
        if (callback != null)
            callback.onAddedSuccessfully(icon);
        callback = null;
    }

    private void setIconImage(boolean isCustomizedHomeIcon) {
        if (isCustomizedHomeIcon) {
            File en_dir = getBasicCustomIconsDirectory(requireContext());
            String path = en_dir.getAbsolutePath();
            Glide.with(context)
                    .load(path + "/" + thisIcon.getIconDrawable() + EXTENSION)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .dontAnimate()
                    .placeholder(R.drawable.ic_board_person)
                    .into(iconImage);
        } else if (thisIcon.isCustomIcon()) {
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
        } else if (v == editBoardIconButton && isCustomizedHomeIcon) {
            firePhotoIntent(0);
        } else if (v == saveButton)
            initSave();
        else if (v == cancelSaveBoard) {
            dismiss();
        }
    }

    private void initSave() {
        if (!iconImageSelected) {
            Toast.makeText(context, getString(R.string.please_select_icon), Toast.LENGTH_SHORT).show();
            return;
        }

        if (titleText.getText().toString().equals("")) {
            Toast.makeText(context, context.getResources().getString(R.string.please_enter_name), Toast.LENGTH_SHORT).show();
            return;
        }
        final int id = (int) Calendar.getInstance().getTimeInMillis();
        String FETCH_ENABLED;
        String IS_PRIMARY;
        Bundle bundle = new Bundle();
        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        if (isCustomizedHomeIcon) {
            bundle.putString(ICON_POSITION, getIconPosition());
            boolean isCategory = (radioGroup.getCheckedRadioButtonId() == R.id.rbIsCategory);
            bundle.putBoolean(BASIC_IS_CATEGORY, isCategory);
            bundle.putBoolean(IS_HOME_CUSTOM_ICON, args.containsKey(IS_HOME_CUSTOM_ICON));
        } else
            bundle.putString(BOARD_ID, boardId);

        Bitmap tempBitmap = null;
        if (iconImage.getDrawable() instanceof BitmapDrawable) {
            tempBitmap = ((BitmapDrawable) iconImage.getDrawable()).getBitmap();
        }
        final Bitmap bitmap = tempBitmap;
        Bitmap croppedBitmap = cropImageView.getCroppedImage();

        if (croppedBitmap == null && selectedLibraryFileName == null) {
            if (addIcon) {
                Toast.makeText(context, getString(R.string.please_crop_image_properly), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final String name = titleText.getText().toString();
        if (addIcon) {
            FETCH_ENABLED = "NULL";
            IS_PRIMARY = "NULL";
            bundle.putSerializable(JELLOW_ID, new JellowIcon(name, String.valueOf(id), -1, -1, id));
        } else {
            JellowIcon iconToPass = new JellowIcon(thisIcon.getIconTitle(), thisIcon.getIconSpeech(), thisIcon.getIconDrawable(), thisIcon.getParent0(), thisIcon.getParent1(), thisIcon.getParent2());
            iconToPass.setVerbiageId(thisIcon.getVerbiageId());
            iconToPass.setType(thisIcon.isCategory() ? BoardConstants.CATEGORY_TYPE : BoardConstants.NORMAL_TYPE);
            iconToPass.setSequenceIcon(thisIcon.isSequenceIcon());
            if (iconToPass.isCustomIcon() && isCustomizedHomeIcon) {
                FETCH_ENABLED = iconToPass.getVerbiageId();
                IS_PRIMARY = "NULL";
                iconToPass.setIconTitle(name);
            } else if (iconToPass.isCustomIcon()) {
                FETCH_ENABLED = iconToPass.getVerbiageId();
                IS_PRIMARY = "NULL";
                iconToPass.setIconTitle(name);
            } else {
                FETCH_ENABLED = iconToPass.getVerbiageId();
                IS_PRIMARY = "TRUE";
                iconToPass.setVerbiageId(id + "");
                iconToPass.setDrawable(id + "");
                iconToPass.setIconTitle(name);
            }
            bundle.putSerializable(JELLOW_ID, iconToPass);
        }

        bundle.putString(DialogAddVerbiage.FETCH_FLAG, FETCH_ENABLED);
        bundle.putString(DialogAddVerbiage.IS_PRIMARY_FLAG, IS_PRIMARY);

        final String verbiageIdForSave = addIcon ? null : ((JellowIcon) bundle.getSerializable(JELLOW_ID)).getVerbiageId();

        DialogAddVerbiage verbiageDialog = DialogAddVerbiage.newInstance(bundle, s -> {
            if (addIcon)
                addNewIcon(id, name, bitmap);
            else
                saveEditedIcon(verbiageIdForSave, name, bitmap);
        });
        verbiageDialog.show(getParentFragmentManager(), DialogAddVerbiage.class.getSimpleName());
        dismiss();
    }

    private String getIconPosition() {
        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        boolean isHomeIcon = args.getBoolean(IS_HOME_CATEGORY, false);
        int levelOneIconPosition = args.getInt(getString(R.string.level_one_intent_pos_tag), -1);
        int levelTwoIconPosition = args.getInt(getString(R.string.level_2_item_pos_tag), -1);
        if (isHomeIcon)
            return "00";
        else if (levelOneIconPosition != -1 && levelTwoIconPosition == -1)
            return "00," + (levelOneIconPosition < 10 ? "0" + levelOneIconPosition : levelOneIconPosition);
        else if (levelOneIconPosition != -1)
            return "00," +
                    (levelOneIconPosition < 10 ? "0" + levelOneIconPosition : levelOneIconPosition) + "," +
                    (levelTwoIconPosition < 10 ? "0" + levelTwoIconPosition : levelTwoIconPosition);
        else
            return "";
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        onClick(null);
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
            args.putString(BoardSearchActivity.SEARCH_MODE, BoardSearchActivity.ICON_SEARCH);
            args.putString(BOARD_ID, boardId);
            BoardSearchActivity searchDialog = BoardSearchActivity.newInstance(args, (icon, resultString) -> {
                if (resultString != null) {
                    iconImageSelected = true;
                    revListener.onPhotoResult(null, LIBRARY_REQUEST, resultString);
                }
            });
            searchDialog.show(getParentFragmentManager(), BoardSearchActivity.class.getSimpleName());
        }
    }

    public boolean hasCameraHardware() {
        return requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public boolean checkPermissionForCamera() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
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
                revListener.onPhotoResult(bitmap, CAMERA_REQUEST, null);
                View container = view.findViewById(R.id.cropContainer);
                if (container != null) container.setVisibility(View.INVISIBLE);
                View cameraCropParent = view.findViewById(R.id.cameraCropParent);
                if (cameraCropParent != null) cameraCropParent.setVisibility(View.INVISIBLE);
            } else {
                Toast.makeText(requireContext(), R.string.please_select_and_adjust_image_first, Toast.LENGTH_SHORT).show();
            }
        });

        ImageView ivBack = view.findViewById(R.id.iv_action_bar_back);
        ivBack.setOnClickListener(v -> {
            View container = view.findViewById(R.id.cropContainer);
            if (container != null) container.setVisibility(View.INVISIBLE);
            View cameraCropParent = view.findViewById(R.id.cameraCropParent);
            if (cameraCropParent != null) cameraCropParent.setVisibility(View.INVISIBLE);
            cropImageView.clearImage();
        });
    }

    private void showImageSourceDialog() {
        Context ctx = new ContextThemeWrapper(requireContext(), R.style.AppTheme);
        final DialogCustom dialog = new DialogCustom(ctx);
        dialog.setText(ctx.getString(R.string.select_image_source));
        dialog.setPositiveText(ctx.getString(R.string.camera));
        dialog.setNegativeText(ctx.getString(R.string.gallery));
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
        // Red background for API 35+ is now handled via statusBarBackground view in XML
        if (Build.VERSION.SDK_INT >= 35) {
            View statusBarBg = view.findViewById(R.id.statusBarBackground);
            if (statusBarBg != null) {
                statusBarBg.setVisibility(View.VISIBLE);
                ViewCompat.setOnApplyWindowInsetsListener(statusBarBg, (v, windowInsets) -> {
                    androidx.core.graphics.Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    ViewGroup.LayoutParams params = v.getLayoutParams();
                    params.height = insets.top;
                    v.setLayoutParams(params);
                    return windowInsets;
                });
            }
            // Ensure system bar icons are white
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(requireActivity().getWindow(), requireActivity().getWindow().getDecorView());
            if (controller != null) {
                controller.setAppearanceLightStatusBars(false);
            }
        }

        // Apply bottom padding to the main cropper container
        View cropContainer = view.findViewById(R.id.cropContainer);
        if (cropContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(cropContainer, (v, windowInsets) -> {
                androidx.core.graphics.Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, 0, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }
    }
}