package dita.pruse.media;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Audio extends Fragment {

    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 222;

    Button record,stopRec;
    EditText fileName;
    MediaRecorder recorder;
    String path;
    ArrayList<File> list;
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View audio = inflater.inflate(R.layout.fragment_audio, container, false);

        record = audio.findViewById(R.id.btnRecord);
        stopRec = audio.findViewById(R.id.btnStopRec);
        fileName = audio.findViewById(R.id.txtFileName);
        listView = audio.findViewById(R.id.listView);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startRecord();
                    stopRec.setEnabled(true);
                    record.setEnabled(false);
                } else {
                    String[] permissionReq = {Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissionReq, RECORD_AUDIO_PERMISSION_REQUEST_CODE);
                }
            }
        });
        stopRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recorder.stop();
                stopRec.setEnabled(false);
                record.setEnabled(true);
            }
        });

        return audio;
    }
    class recordAdapter extends BaseAdapter {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.list_items,parent ,false);
            TextView audio = convertView.findViewById(R.id.textView);
            audio.setText(list.get(position).toString());

            return convertView;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            // we have heard back from audio recorder
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startRecord();
            } else {
                Toast.makeText(getActivity(), getString(R.string.noRecord), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void startRecord() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/"+timestamp+"_audio_record.3gp";
        String filename=path.substring(path.lastIndexOf("/")+1);
        setupMediaRecorder();
        try{
            recorder.prepare();
            recorder.start();
        }catch (Exception e){
            e.printStackTrace();
        }
        Toast.makeText(getActivity(),getString(R.string.recording),Toast.LENGTH_SHORT).show();
        fileName.setText(filename);
        list = audioReader( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        listView.setAdapter( new recordAdapter());

    }
    private ArrayList<File> audioReader(File externalStoragePublicDirectory) {
        ArrayList<File> mlist = new ArrayList<>();

        File[] files = externalStoragePublicDirectory.listFiles();
        for (int i = 0;i<files.length;i++){
            if(files[i].isDirectory()){
                mlist.addAll(audioReader(files[i]));
            }else{
                if(files[i].getName().endsWith(".3gp")){
                    mlist.add(files[i]);
                }
            }
        }
        return  mlist;
    }
    private void setupMediaRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setOutputFile(path);
    }
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        audioDeleter(file);
    }

    private void audioDeleter(File file) {
        if (file.exists()) {
            File[] theData = file.listFiles();
            for (int i = 0; i < theData.length; i++) {
                File oneFile = theData[i];
                if (oneFile.isDirectory()) {
                } else {
                    if (oneFile.getName().endsWith(".3gp")) {
                        oneFile.delete();
                    }
                }
            }
        }
    }
}
