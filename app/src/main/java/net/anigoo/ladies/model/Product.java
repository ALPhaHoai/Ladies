package net.anigoo.ladies.model;
import android.util.Log;

import net.anigoo.ladies.lib.DB;
import net.anigoo.ladies.lib.DateTimeLib;
import net.anigoo.ladies.lib.lib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
public class Product  implements Serializable {
    public long id;
    public String name;
    public int price;
    public String description;
    public String image;
    public String image_small;
    public Date date_created;
	
	public Product(long id){
		this.id = id;
	}

    public void getFullInfo(DB db){
		if(this.id < 1) {this.id = -1; return ;}
        if(db.select("name, price, description, image, image_small, date_created", "product", "id = '" + this.id + "'")){
            this.name = db.firstKq.get(0);
			if(lib.checkLong(db.firstKq.get(1))){
				this.price = Integer.valueOf(db.firstKq.get(1));
			}
            this.description = db.firstKq.get(2);
            this.image = db.firstKq.get(3);
            this.image_small = db.firstKq.get(4);
            this.date_created = DateTimeLib.convertStringtoDate("yyyy-MM-dd HH:mm:ss", db.firstKq.get(5));
        } else {
            this.id = -1;
        }
    }
    //Lấy danh sách các comment của sản phẩm này
    public Comments getComments(DB db){
	    if(db.select("comments.id, comments.cmt, comments.user_id, users.name, users.avatar", "comments, users","comments.user_id = users.id AND comments.product_id = '" + this.id + "' ORDER BY comments.created DESC LIMIT 20")){
	        ArrayList<ArrayList<String>> kq  = db.kq;
	        Comments comments = new Comments();
	        for(int i = 0; i < kq.size(); i++){
	            try{
                    Comment comment = new Comment();
                    comment.id = Integer.valueOf(kq.get(i).get(0));
                    comment.content = kq.get(i).get(1);
                    User user = new User(Integer.valueOf(kq.get(i).get(2)));
                    user.name = kq.get(i).get(3);
                    user.avatar = kq.get(i).get(4);
                    comment.user = user;
                    comments.add(comment);
                } catch (Exception ex){}
            }
            return comments;
        }
        return null;
    }
    //Lấy tổng số upvote của sản phẩm này
    public int getTotalUpvote(DB db){
	    try{
            if(db.select("count(*)","user_vote","product_id = '" + this.id + "' AND vote = 'up'")){
                return Integer.valueOf(db.firstKq.get(0));
            }
            return 0;
        } catch (Exception ex){
	        return 0;
        }
    }
    //Lấy tổng số downvote của sản phẩm này
    public int getTotalDownvote(DB db){
        try{
            if(db.select("count(*)","user_vote","product_id = '" + this.id + "' AND vote = 'down'")){
                return Integer.valueOf(db.firstKq.get(0));
            }
            return 0;
        } catch (Exception ex){
            return 0;
        }
    }


    //Lấy danh sách các sản phẩm tương tự
    public Products getSimilar(DB db){
        if(this.id < 1) return null;
        if(db.select("product.id, product.name, product.price, product.description, product.image, product.image_small, product.date_created", "product, similar_product", "product.id = similar_product.similar_product_id AND similar_product.product_id = '" + this.id + "' LIMIT 6")){
            ArrayList<ArrayList<String>> products = db.kq;
            Products sProducts = new Products();
            for(ArrayList<String> product : products){
                Product sProduct = new Product(Long.valueOf(product.get(0)));
                sProduct.name = product.get(1);
                if(lib.checkLong(product.get(2))){
                    sProduct.price = Integer.valueOf(product.get(2));
                }
                sProduct.description = product.get(3);
                sProduct.image = product.get(4);
                sProduct.image_small = product.get(5);
                sProduct.date_created = DateTimeLib.convertStringtoDate("yyyy-MM-dd HH:mm:ss", product.get(6));
                sProducts.add(sProduct);
            }
            return sProducts;
        }
        return null;
    }
}
