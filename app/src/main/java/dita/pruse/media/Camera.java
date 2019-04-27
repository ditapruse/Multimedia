package dita.pruse.media;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Camera extends Fragment  {

    private static final int REQUEST_IMAGE_CAPTURE = 1888;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1337;
    GridView gridView;
    ImageView imageView;
    ArrayList<File> arrayList;
    String photoPath;

    float angle = (float) 90.00;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View cameraView = inflater.inflate(R.layout.fragment_camera, container, false);
        Button takePicture = cameraView.findViewById(R.id.btnTakePic);
        gridView = cameraView.findViewById(R.id.gridView);
        imageView = (ImageView) cameraView.findViewById(R.id.imgLast);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(getActivity(), "This is my Toast message!", Toast.LENGTH_LONG).show();
                    invokeCamera();
                } else {
                    String[] permissionRequest = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissionRequest, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }
        });
        return cameraView;
    }

    private ArrayList<File> imageReader(File externalStoragePublicDirectory) {
        ArrayList<File> listOfArray = new ArrayList<>();

        File[] files = externalStoragePublicDirectory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                listOfArray.addAll(imageReader(files[i]));
            } else {
                if (files[i].getName().endsWith(".jpg")) {
                    listOfArray.add(files[i]);
                }
            }
        }
        return listOfArray;
    }
    class GridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.my_grid, parent, false);
            ImageView imageV = convertView.findViewById(R.id.imageView2);
            imageV.setImageURI(Uri.parse(getItem(position).toString()));

            return convertView;
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(reqCode, permissions, grantResults);
        if (reqCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                invokeCamera();
            } else {
                Toast.makeText(getActivity(), getString(R.string.noCamera), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void invokeCamera() {
        Uri pictureUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getApplicationContext().getPackageName() + ".provider", createImageFile());

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);

        intent.setFlags(intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private File createImageFile() {
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        SimpleDateFormat date = new SimpleDateFormat("yyyMMdd_HHmmss");
        String timestamp = date.format(new Date());

        File img = new File(pictureDirectory, "picture" + timestamp + ".jpg");
        photoPath = img.getAbsolutePath();
        return img;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: {
                    if (resultCode == Activity.RESULT_OK) {
                        File file = new File(photoPath);
                        Bitmap bitmap = MediaStore.Images.Media
                                .getBitmap(getContext().getContentResolver(), Uri.fromFile(file));
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            imageView.setRotation(angle);
                        }
                    }
                    arrayList = imageReader(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
                    gridView.setAdapter(new GridAdapter());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        imageDeleter(file);
    }

    private void imageDeleter(File file) {
        if(file.exists()){
            File[] theData = file.listFiles();
            for (int i = 0; i < theData.length; i++) {
                File filee = theData[i];
                if (filee.isDirectory()) {
                } else {
                    if (filee.getName().endsWith(".jpg")) {
                        filee.delete();
                    }
                }
            }
        }
    }
}
