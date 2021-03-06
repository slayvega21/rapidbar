package pe.edu.upc.rapidbar.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pe.edu.upc.rapidbar.R;
import pe.edu.upc.rapidbar.adapters.OrderChefAdapter;
import pe.edu.upc.rapidbar.helpers.ChefRecyclerTouchHelper;
import pe.edu.upc.rapidbar.models.Order;
import pe.edu.upc.rapidbar.models.SharedPreferencesAccess;
import pe.edu.upc.rapidbar.models.UserLogin;
import pe.edu.upc.rapidbar.network.RapidBarApiService;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChefFragment extends Fragment implements ChefRecyclerTouchHelper.ChefRecyclerTouchHelperListener {
    RecyclerView ordersChefRecyclerView;
    OrderChefAdapter ordersChefAdapter;
    RecyclerView.LayoutManager ordersChefLayoutManager;
    List<Order> orders;

    public ChefFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        updateData();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chef, container, false);

        //RecyclerView Setup
        ordersChefRecyclerView = (RecyclerView) view.findViewById(R.id.chefRecyclerView);
        orders = new ArrayList<>();

        ordersChefAdapter = new OrderChefAdapter(orders);

        ordersChefLayoutManager = new LinearLayoutManager(view.getContext());
        ordersChefRecyclerView.setLayoutManager(ordersChefLayoutManager);

        ordersChefRecyclerView.setAdapter(ordersChefAdapter);

        ordersChefRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ordersChefRecyclerView.addItemDecoration(new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ChefRecyclerTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(ordersChefRecyclerView);

        return view;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof OrderChefAdapter.OrderChefViewHolder) {
            // get the removed item name to display it in snack bar
            String name = orders.get(viewHolder.getAdapterPosition()).getId();

            // backup of removed item for undo purpose
            final Order deletedItem = orders.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            ordersChefAdapter.removeItem(viewHolder.getAdapterPosition());

            String idtodelete = deletedItem.getId();
            AndroidNetworking.post("http://52.15.243.101/api/order/"+idtodelete)// posting json
                    .setTag(getString(R.string.app_name))
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {

                        }

                        @Override
                        public void onError(ANError anError) {

                        }
                    });
            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(this.getView(), "Orden N: " + name + " completada!", Snackbar.LENGTH_LONG);
            snackbar.setAction("DESHACER", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    ordersChefAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
    }
    private void updateData() {

        //SETEAR URL PARA FILTRO
        UserLogin userLogin = SharedPreferencesAccess.LoadUserLogin(this.getContext());
        String useurl = "http://52.15.243.101/api/employee/"+userLogin.getId()+"/order";



            AndroidNetworking.get(useurl)
                    .setPriority(Priority.HIGH.LOW)
                    .setTag(getString(R.string.app_name))
                    .build()
                    .getAsJSONArray(new JSONArrayRequestListener() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                orders = Order.from(response);
                                ordersChefAdapter.setOrders(orders);
                                ordersChefAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onError(ANError anError) {

                        }
                    });

    }
}
