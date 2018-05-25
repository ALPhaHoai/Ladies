package net.anigoo.ladies.model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class Activities extends ArrayList<Activity>  implements Serializable {
    //Sắp xếp danh sách các hoạt động theo ngày tháng diễn ra
    public void softByDate(){
        if(this.size() <= 1) return ;
        for (int i = 0; i < this.size() - 1; i ++){
            for (int j = i + 1; j < this.size() ; j ++){
                if(this.get(j).time.getTime() > this.get(i).time.getTime())  {
                    Activity Aci = this.get(i);
                    Activity Acj = this.get(j);
                    this.set(i, Acj);
                    this.set(j, Aci);
                }
            }
        }
    }
}
