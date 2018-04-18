package game;

import autoencoder.Encoder;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class ManuelFeatures extends Encoder
{

    @Override
    public void build(final int encoder_size, final int input_size)
    {
        _encoder_size  = 10;
        _encoder_ready = true;
    }

    @Override
    public double[] encoding(final int[] input)
    {
        int field_width = input[1];
        int field_height = input[2];

        //calc height of each column
        int[]height_map = new int[field_width];
        //calc number of all blocks!
        int num_blocks = 0;                                                //FEATURE!
        //calc number of weighted sum of blocks!
        int weighted_blocks = 0;                                           //FEATURE!
        //FOR LOOP OVER ENTIRE FIELD!!
        for (int i =0; i<field_width;i++)
        {
            for (int j=0; j<field_height;j++)
            { //go over all possible heights!
                if (input[3+i+j*field_width]!=0){
                    height_map[i]=j;
                    num_blocks = num_blocks+1;
                    weighted_blocks = weighted_blocks + (j+1);
                }
            }
        }

        int max_pile_height = height_map[0];                               //FEATURE!
        for (int i =1; i<field_width;i++){
            if(height_map[i]>max_pile_height){max_pile_height=height_map[i];}
        }

        int min_pile_height = height_map[0];
        for (int i =1; i<field_width;i++){
            if(height_map[i]<min_pile_height){min_pile_height=height_map[i];}
        }

        int max_altitude_difference = max_pile_height - min_pile_height;           //FEATURE!

        //calculate a map of "wells":
        int[]wells_map = new int[field_width];
        for (int i =0; i<field_width;i++){
            if(i==0){
                if(height_map[1]>height_map[0]){wells_map[0] = height_map[1]-height_map[0];}
                else {wells_map[0]=0;}
            }
            else if(i==(field_width-1)){
                if(height_map[field_width-2]>height_map[field_width-1]){wells_map[field_width-1] = height_map[field_width-2]-height_map[field_width-1];}
                else {wells_map[field_width-1]=0;}
            }
            else {
                if (height_map[i-1]>height_map[i] & height_map[i+1]>height_map[i]){
                    wells_map[i]= min(height_map[i-1]-height_map[i],height_map[i+1]-height_map[i]);
                }
                else {wells_map[i]=0;}
            }
        }

        int max_well_depth = wells_map[0];                                         //FEATURE!
        int sum_of_wells = 0;                                                      //FEATURE!
        for (int i =1; i<field_width;i++){
            sum_of_wells = sum_of_wells + wells_map[i];
            if(wells_map[i]>max_well_depth){max_well_depth=wells_map[i];}
        }




        // calc connected number of holes and all holes
        int conn_num_holes = 0;                                                   //FEATURE!
        int total_num_holes = 0;                                                  //FEATURE!
        for (int i =0; i<field_width*(field_height-1)-1;i++)
        {
            int calc = input[3+i]-input[3+field_width+i]; //lower - upper
            //if above there is one but below not -> will yield to -1!!!
            if (calc<0) {conn_num_holes++;}
            if((input[3+i]==0) & ((i/field_width)<height_map[i%field_width])){total_num_holes++;}
        }

        //calculate horizontal transitions: (slightly other defined than paper)
        int hor_transitions = 0;                                                //FEATURE!
        //FOR LOOP OVER ENTIRE FIELD!!
        for (int i =0; i<field_height;i++)
        {
            for (int j=0; j<(field_width-1);j++)
            {
                hor_transitions = hor_transitions + abs(input[3+j+i*field_width]-input[3+j+1+i*field_width]);
            }
        }

        //calculate vertical transitions: (slightly other defined than paper)
        int ver_transitions = 0;                                              //FEATURE
        //FOR LOOP OVER ENTIRE FIELD!!
        for (int i =0; i<field_width;i++)
        {
            for (int j=0; j<(field_height-1);j++)
            {
                ver_transitions = ver_transitions + abs(input[3+i+j*field_width]-input[3+i+(j+1)*field_width]);
            }
        }


        //from there extract: aggregate height:
        int aggregate_height = 0;                                         //FEATURE
        for (int j=0; j<field_width; j++){
            aggregate_height = aggregate_height + height_map[j];
        }

        //from there extract: bumpieness:
        int bumpieness = 0;                                            //FEATURE!!
        for (int j=1; j<field_width; j++)
        {
            bumpieness = bumpieness + abs(height_map[j]-height_map[j-1]);
        }

        //TODO: INCLUDE DIFFERENCE FEATURES SEE WHEN TREES FALL WEBSITE!!!

        //return new double[]{total_num_holes,num_cleared_rows,aggregate_height,bumpieness};
        //new: 11 features from paper!!!
        return new double[]{max_pile_height,total_num_holes,conn_num_holes,max_altitude_difference,max_well_depth,sum_of_wells,num_blocks,weighted_blocks,hor_transitions,ver_transitions};
    }

    @Override
    public int flatten(final int[] input)
    {
        return -1;
    }

}
