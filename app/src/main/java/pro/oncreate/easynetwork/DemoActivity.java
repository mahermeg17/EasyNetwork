package pro.oncreate.easynetwork;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pro.oncreate.easynet.NBuilder;
import pro.oncreate.easynet.data.NConst;
import pro.oncreate.easynet.data.NErrors;
import pro.oncreate.easynet.models.NKeyValueModel;
import pro.oncreate.easynet.models.NRequestModel;
import pro.oncreate.easynet.models.NResponseModel;
import pro.oncreate.easynet.tasks.NCallbackParse;
import pro.oncreate.easynet.tasks.NTask;
import pro.oncreate.easynetwork.adapters.ExpandableListAdapter;
import pro.oncreate.easynetwork.models.CountryModel;

public class DemoActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar progressBar;
    private EditText edtToolbar;

    private ExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        edtToolbar = (EditText) findViewById(R.id.toolbar_edittext);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ImageButton ibtnGo = (ImageButton) findViewById(R.id.toolbar_go);
        RecyclerView recyclerview = (RecyclerView) findViewById(R.id.recyclerview);

        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new ExpandableListAdapter(new ArrayList<ExpandableListAdapter.Item>());
        recyclerview.setAdapter(adapter);

        ibtnGo.setOnClickListener(this);
        ibtnGo.performClick();
    }

    @Override
    public void onClick(View view) {
        NBuilder.create()
                .setPath("countries/100")
                .addParam("expand", "name")
                .setMethod(NBuilder.GET) // default
                .enableDefaultListeners(true) // default
                .setReadTimeout(NTask.DEFAULT_TIMEOUT_READ) // default
                .setConnectTimeout(NTask.DEFAULT_TIMEOUT_CONNECT) // default
                .setContentType(NConst.MIME_TYPE_X_WWW_FORM_URLENCODED) // default
                .startWithParse(new NCallbackParse<CountryModel>(CountryModel.class) {
                    @Override
                    public void onStart(NRequestModel requestModel) {
                        edtToolbar.setText(requestModel.getMethod() + " " + requestModel.getUrl());
                        progressBar.setVisibility(View.VISIBLE);

                        adapter.clear();
                        adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, String.format(Locale.getDefault(), "Headers (%d)", requestModel.getHeaders().size())));
                        for (NKeyValueModel header : requestModel.getHeaders())
                            adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, header.getKey() + ": " + header.getValue()));

                        adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, String.format(Locale.getDefault(), "Params (%d)", requestModel.getParams().size())));
                        for (NKeyValueModel header : requestModel.getParams())
                            adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, header.getKey() + ": " + header.getValue()));
                    }

                    @Override
                    public void onSuccess(CountryModel model, NResponseModel responseModel) {
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(getCurrentFocus(), String.format(Locale.getDefault(), "%s [status code: %d] [response time: %d ms]", "Success", responseModel.getStatusCode(), responseModel.getResponseTime()), Snackbar.LENGTH_LONG).show();

                        adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, String.format(Locale.getDefault(), "Headers (%d)", responseModel.getHeaders().size())));
                        for (Map.Entry<String, List<String>> entry : responseModel.getHeaders().entrySet())
                            adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, entry.getKey() + ": " + entry.getValue().toString()));

                        adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Body"));
                        adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, responseModel.getBody()));
                    }

                    // Use this overload if API return collection models
//                    @Override
//                    public void onSuccess(ArrayList<CountryModel> models, NResponseModel responseModel) {
//                    }

                    @Override
                    public void onError(NResponseModel responseModel) {
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(getCurrentFocus(), String.format(Locale.US, "%s [status code: %d] [response time: %d ms]", "Error", responseModel.getStatusCode(), responseModel.getResponseTime()), Snackbar.LENGTH_LONG).show();

                        adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Headers"));
                        for (Map.Entry<String, List<String>> entry : responseModel.getHeaders().entrySet())
                            adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, entry.getKey() + ": " + entry.getValue().toString()));

                        adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Response"));
                        adapter.addItem(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, responseModel.getBody()));
                    }

                    @Override
                    public void onFailed(NRequestModel nRequestModel, NErrors error) {
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(getCurrentFocus(), String.format(Locale.getDefault(), "Failed %s ", error.name()), Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}
