package tests.CTB;

import autoencoder.Encoder;

import java.util.Arrays;
import static java.lang.Math.abs;

public class ManuelFeaturesCTB extends Encoder
{

    @Override
    public void build(final int encoder_size, final int input_size)
    {
        _encoder_size  = 1;
        _encoder_ready = true;
    }

    @Override
    public double[] encoding(final int[] input)
    {
        int[] firstHalf = Arrays.copyOfRange(input, 0, input.length/2);
        int[] secondHalf = Arrays.copyOfRange(input, input.length/2, input.length);
        int catcher_pos = -1;
        int ball_pos    = -1;
        for (int i=0; i < (input.length/2); i++)
        {
            if (firstHalf[i]!=0){catcher_pos=i;}
            if (secondHalf[i]!=0){ball_pos=i;}
        }
        return new double[]{catcher_pos - ball_pos + CTBConstants.field_width};
    }

    @Override
    public int flatten(final int[] input)
    {
        return (int)encoding(input)[0];
    }

}
