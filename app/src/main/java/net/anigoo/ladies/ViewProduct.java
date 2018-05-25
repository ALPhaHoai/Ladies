package net.anigoo.ladies;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.anigoo.ladies.lib.DB;
import net.anigoo.ladies.lib.lib;
import net.anigoo.ladies.model.Comments;
import net.anigoo.ladies.model.Product;
import net.anigoo.ladies.model.Products;
import net.anigoo.ladies.model.User;

public class ViewProduct extends AppCompatActivity {
    User user;
    Product product;
    Products similarProducts;
    DB db;
    Boolean user_scan;//Có phải người dùng scan sản phẩm rồi tới đây không (để update history)

    ImageView imageProduct;
    TextView productTitle, productPrice, productDescription;
    LinearLayout similarProductLayout, similarProductItems, commentsBlock;

    Button upvoteButton, downvoteButton;
    ImageButton commentButton;


    ProgressDialog progressDialog;
    Dialog imageProductDialog;//Hiển thị popup ảnh sản phẩm
    Dialog commentDialog;

    boolean userUpvote = false, userDownvote = false;
    int totalUpvote = 0, totalDownvote = 0;
    Comments comments = null;

    Button btn_comment, btm_cancel;
    String commentInput;

    String previousScreen = "ViewUserProfile";
    private float x1,x2;
    static final int MIN_DISTANCE = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageProductDialog = new Dialog(this);
        commentDialog = new Dialog(this);
        if(getIntent().hasExtra("user")){
            user = (User)getIntent().getSerializableExtra("user");
        }
        if(getIntent().hasExtra("prev")){
            previousScreen = (String)getIntent().getSerializableExtra("prev");
        }
        user_scan = false;
        if(getIntent().hasExtra("user_scan") && ((Boolean)getIntent().getSerializableExtra("user_scan")).equals(true)){
            user_scan = true;
        }
        db = new DB();
        if(getIntent().hasExtra("product")){
            String productId = (String)getIntent().getSerializableExtra("product");
            if(lib.checkLong(productId)){
                setContentView(R.layout.activity_view_product);
                progressDialog=new ProgressDialog(this);
                product = new Product(Long.valueOf(productId));
                new BeforeViewProduct().execute();
            } else Toast.makeText(getBaseContext(),"Không tìm thấy sản phẩm",Toast.LENGTH_LONG).show();
        }

        imageProduct = (ImageView)findViewById(R.id.imageProduct) ;
        productTitle = (TextView)findViewById(R.id.productTitle) ;
        productPrice = (TextView)findViewById(R.id.productPrice) ;
        productDescription = (TextView)findViewById(R.id.productDescription) ;
        similarProductLayout = (LinearLayout) findViewById(R.id.productSimilar);
        similarProductItems = (LinearLayout) findViewById(R.id.similarProductItems);

        upvoteButton = (Button)findViewById(R.id.upvoteButton);
        downvoteButton = (Button)findViewById(R.id.downvoteButton);
        commentButton = (ImageButton)findViewById(R.id.commentButton);

        commentsBlock = (LinearLayout)findViewById(R.id.comments);

        if(null != imageProduct){
        imageProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
        }



    }

    //Hiển thị popup ảnh sản phẩm khi click vào ảnh sản phẩm
    public void showPopup(View v) {
        TextView txtclose;
        imageProductDialog.setContentView(R.layout.custompopup);
        txtclose =(TextView) imageProductDialog.findViewById(R.id.txtclose);
        if(product != null && product.image != null && product.image.length() > 4){
        Picasso.with(ViewProduct.this).load(product.image).into((ImageView) imageProductDialog.findViewById(R.id.popupImage));
        }
        txtclose.setText("X");
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageProductDialog.dismiss();
            }
        });
        imageProductDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imageProductDialog.show();
    }
    //Hiển thị popup ảnh sản phẩm khi click vào nút comment
    public void showPopupComment(View v) {
        if(null != user) {
            commentDialog.setContentView(R.layout.comment_popup);
            Button btn_comment = (Button) commentDialog.findViewById(R.id.btn_comment);
            Button btm_cancel = (Button) commentDialog.findViewById(R.id.btn_cancel);

            btn_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText input = (EditText) commentDialog.findViewById(R.id.commentInput);
                    commentInput = input.getText().toString();
                    new SendComment().execute();
                }
            });
            btm_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentDialog.dismiss();
                }
            });
            commentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            commentDialog.show();
        } else {
            Toast.makeText(getBaseContext(),"Bạn phải đăng nhập trước khi bình luận",Toast.LENGTH_LONG).show();
        }
    }
    private class SendComment extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Sending...");
            progressDialog.show();
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            user.addComment(String.valueOf(product.id), commentInput , db);
            comments = product.getComments(db);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            loadComments(comments);
            progressDialog.hide();
            commentDialog.dismiss();
        }
    }


    //Xem sản phẩm liên quan khi click vào 1 item sản phẩm liên quan
    private void viewProduct(View v){
        String productId = String.valueOf(v.getTag());
        Intent intent = new Intent(ViewProduct.this, ViewProduct.class);
        intent.putExtra("product", (productId));
        intent.putExtra("user", user);
        if("SplashActivity".equals(previousScreen)) intent.putExtra("prev", previousScreen);
        startActivity(intent);
    }

    //Load thông tin sản phẩm
    private class BeforeViewProduct extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading...");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            product.getFullInfo(db);
            if(product.id != -1){
            similarProducts = product.getSimilar(db);
                comments = product.getComments(db);
            if(user != null ){
                if(user_scan){//Nếu là người dùng quét sản phẩm này thì update history
                user.addUserHistory(String.valueOf(product.id), db);
                }
                userUpvote = user.isUpvote(String.valueOf(product.id), db);
                if(userUpvote == false){
                    userDownvote = user.isDownvote(String.valueOf(product.id), db);
                }
                totalUpvote = product.getTotalUpvote(db);
                totalDownvote = product.getTotalDownvote(db);
            }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(product.id == -1){
                Toast.makeText(getBaseContext(),"Không tìm thấy sản phẩm",Toast.LENGTH_LONG).show();
                ViewProduct.this.finish();
            }
            else {
                productTitle.setText(lib.splitTitle(lib.titleCase(product.name), 26));
                productPrice.setText(product.price + " VNĐ");

                if(product.description != null && product.description.length() > 0)
                {
                    productDescription.setText(product.description);
                } else {
                    findViewById(R.id.productDescriptionBlock).setVisibility(View.GONE);
                    findViewById(R.id.descriptionTitle).setVisibility(View.GONE);
                }

                //Load ảnh sản phẩm
                if (product.image != null && product.image.length() > 4) {
                    Picasso.with(ViewProduct.this).load(product.image).into(imageProduct);
                }

                //Load danh sách các sản phẩm liên quan
                if (similarProducts != null && similarProducts.size() > 0) {
                    for (int i = 0; i < 6; i++) {
                        View v = similarProductItems.getChildAt(i);
                        if (i < similarProducts.size()) {
                            v.setTag(similarProducts.get(i).id);
                            v.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    viewProduct(view);
                                }
                            });
                            ImageView image = (ImageView) v.findViewWithTag("similarProductItemImage");
                            if (null != image && null != similarProducts.get(i).image && similarProducts.get(i).image.length() > 4) {
                                Picasso.with(ViewProduct.this).load(similarProducts.get(i).image).into(image);
                            }
                            TextView title = (TextView) v.findViewWithTag("similarProductItemTitle");
                            if (null != title)
                                title.setText(lib.splitTitle(lib.titleCase(similarProducts.get(i).name), 20));
                            TextView price = (TextView) v.findViewWithTag("similarProductItemPrice");
                            price.setText(String.valueOf(similarProducts.get(i).price) + " VNĐ");

                        } else {
                            v.setVisibility(View.GONE);
                        }
                    }
                } else {
                    similarProductLayout.setVisibility(View.GONE);
                }


                //Setup trạng thái upvote/downvote comment
                if(null != user){
                    if(userUpvote){
                        setUserUpvote();
                    }
                    else if(userDownvote){
                        setUserDownvote();
                    }
                }

                if(totalUpvote > 0) setTotalUpvote(totalUpvote);
                if(totalDownvote > 0) setTotalDownvote(totalDownvote);

                //Load danh sách comments
                loadComments(comments);
            }
            progressDialog.hide();

        }
    }
    private class UserUpvoteProduct extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            user.upvote(String.valueOf(product.id), db);
            return null;
        }

    }
    private class UserDownvoteProduct extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            user.downvote(String.valueOf(product.id), db);
            return null;
        }

    }
    private class UserRemoveDownvoteProduct extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            user.removeDownvote(String.valueOf(product.id), db);
            return null;
        }
    }
    private class UserRemoveUpvoteProduct extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            user.removeUpvote(String.valueOf(product.id), db);
            return null;
        }
    }

    public void loadComments(Comments comments){
        if(null != comments && comments.size() > 0) {
            findViewById(R.id.commentTitle).setVisibility(View.VISIBLE);
            commentsBlock.setVisibility(View.VISIBLE);
            for(int i = 0; i  < 20 ; i++){
                View childAt = commentsBlock.getChildAt(i);
                if(i < comments.size()){
                    childAt.setVisibility(View.VISIBLE);
                    ImageView commentUserAvatar = (ImageView)childAt.findViewWithTag("commentUserAvatar");
                    if(comments.get(i).user != null && comments.get(i).user.avatar != null){
                        Picasso.with(ViewProduct.this).load(comments.get(i).user.avatar).into(commentUserAvatar);
                    }
                    TextView commentUserName = (TextView)childAt.findViewWithTag("commentUserName");
                    commentUserName.setText(comments.get(i).user.name);
                    TextView commentContent = (TextView)childAt.findViewWithTag("commentContent");
                    commentContent.setText(comments.get(i).content);

                } else {
                    childAt.setVisibility(View.INVISIBLE);
                }
            }
        }else {
            findViewById(R.id.commentTitle).setVisibility(View.INVISIBLE);
            commentsBlock.setVisibility(View.INVISIBLE);
        }
    }

    public void setUserUpvote(){
        upvoteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked_button, 0, 0, 0);
        downvoteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dislike_button, 0, 0, 0);
    }
    public void setTotalUpvote(int total){
        upvoteButton.setText(String.valueOf(total));
    }
    public void setUserDownvote(){
        upvoteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_button, 0, 0, 0);
        downvoteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.disliked_button, 0, 0, 0);
    }
    public void setTotalDownvote(int total){
        downvoteButton.setText(String.valueOf(total));
    }
    public void upvote(View v){
        if(user == null) {Toast.makeText(getBaseContext(),"Bạn phải đăng nhập trước",Toast.LENGTH_LONG).show();return ;}
        if(userUpvote){//remove upvote
            userUpvote = false;
            if(totalUpvote > 0)totalUpvote --;
            upvoteButton.setText(String.valueOf(totalUpvote));
            upvoteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_button, 0, 0, 0);
            new UserRemoveUpvoteProduct().execute();
        } else {
        setUserUpvote();
        if(userDownvote){
            if(totalDownvote > 0)totalDownvote --;
            downvoteButton.setText(String.valueOf(totalDownvote));
        }
        totalUpvote ++;
        upvoteButton.setText(String.valueOf(totalUpvote));
        userUpvote = true;
        userDownvote = false;

            new UserUpvoteProduct().execute();
    }
    }
    public void downvote(View v){
        if(user == null) {Toast.makeText(getBaseContext(),"Bạn phải đăng nhập trước",Toast.LENGTH_LONG).show();return ;}
        if(userDownvote){//remove downvote
            userDownvote = false;
            if(totalDownvote > 0 )totalDownvote --;
            downvoteButton.setText(String.valueOf(totalDownvote));
            downvoteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dislike_button, 0, 0, 0);
            new UserRemoveDownvoteProduct().execute();
        }
        else {
        new UserDownvoteProduct().execute();
        setUserDownvote();
        if(userUpvote){
            if(totalUpvote > 0) totalUpvote --;
            upvoteButton.setText(String.valueOf(totalUpvote));
        }
        totalDownvote ++;
        downvoteButton.setText(String.valueOf(totalDownvote));
        userDownvote = true;
        userUpvote = false;
    }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;

                if (Math.abs(deltaX) > MIN_DISTANCE)
                {
                    // Left to Right swipe action
                    if (x2 > x1)
                    {
                        switch (previousScreen) {
                            case "ViewUserProfile" :{
                                Intent intent=new Intent(ViewProduct.this,ViewUserProfile.class);
                                if(null != user) intent.putExtra("user",user);
                                startActivity(intent);
                                break;
                            }
                            case "SplashActivity" :{
                                Intent intent=new Intent(ViewProduct.this,SplashActivity.class);
                                if(null != user) intent.putExtra("user",user);
                                startActivity(intent);
                                break;
                            }
                            case "RecentlyActivity" :{
                                Intent intent=new Intent(ViewProduct.this,RecentlyActivity.class);
                                if(null != user) intent.putExtra("user",user);
                                startActivity(intent);
                                break;
                            }
                        }

                    }

                    // Right to left swipe action
                    else
                    {
//                        Toast.makeText(this, "Right to Left swipe [Previous] \n" + previousScreen, Toast.LENGTH_LONG).show ();
                    }

                }
                else
                {
                    // consider as something else - a screen tap for example
                }
                break;
        }
        return super.onTouchEvent(event);
    }

}
