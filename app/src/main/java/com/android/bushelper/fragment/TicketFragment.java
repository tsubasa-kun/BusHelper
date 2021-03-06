package com.android.bushelper.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.bushelper.R;
import com.android.bushelper.activity.TicketDetailActivity;
import com.android.bushelper.adapter.TicketAdapter;
import com.android.bushelper.app.APIs;
import com.android.bushelper.bean.TicketBean;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

public class TicketFragment extends Fragment {

    private EditText ticketFromET;
    private EditText ticketToET;
    private Button ticketSearchBtn;
    private ListView ticketList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ticket, container, false);
        ticketFromET = (EditText)root.findViewById(R.id.ticket_from_et);
        ticketToET = (EditText)root.findViewById(R.id.ticket_to_et);
        ticketSearchBtn = (Button)root.findViewById(R.id.ticket_search_btn);
        ticketList = (ListView)root.findViewById(R.id.ticket_list);
        ticketSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTicket();
            }
        });
        return root;
    }

    public void searchTicket() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ticketFromET.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(ticketToET.getWindowToken(), 0);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        String fromStr = ticketFromET.getText().toString();
        String toStr = ticketToET.getText().toString();
        if (TextUtils.isEmpty(fromStr)) {
            Toast.makeText(getActivity(), "请输入出发地", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(toStr)) {
            Toast.makeText(getActivity(), "请输入目的地", Toast.LENGTH_SHORT).show();
        } else {
            RequestParams requestParams = new RequestParams(APIs.TICKET);
            requestParams.addQueryStringParameter("key", APIs.COACH_KEY);
            requestParams.addQueryStringParameter("from", fromStr);
            requestParams.addQueryStringParameter("to", toStr);
            x.http().get(requestParams, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Gson gson = new Gson();
                    TicketBean ticketBean = gson.fromJson(result, TicketBean.class);
                    initTicketList(ticketBean);
                    progressDialog.dismiss();
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(CancelledException cex) {
                    progressDialog.dismiss();
                }

                @Override
                public void onFinished() {
                    progressDialog.dismiss();
                }
            });
        }
    }

    public void initTicketList(final TicketBean ticketBean) {
        if (ticketBean.getResult() != null) {
            TicketAdapter ticketAdapter = new TicketAdapter(getActivity(), ticketBean);
            ticketList.setAdapter(ticketAdapter);
            ticketList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), TicketDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("ticket", ticketBean.getResult().getList().get(position));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        } else {
            Toast.makeText(getActivity(), "没有查到相关票务", Toast.LENGTH_SHORT).show();
        }
    }

}
