package autoencoder;

public class BinaryFlattening extends Encoder
{
    @Override
    public void build(final int encoder_size, final int input_size)
    {
        _encoder_size  = encoder_size;
        _encoder_ready = true;
    }

    @Override
    public double[] encoding(final int[] input)
    {
        return null;
    }

    @Override
    public int flatten(final int[] input)
    {
        int score = 0, exp = 1;
        for (int cell : input)
        {
            score += exp*(cell > 0 ? 1 : 0);
            exp   *= 2;
        }
        return score;
    }
}
