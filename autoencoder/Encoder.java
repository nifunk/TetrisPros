package autoencoder;

public abstract class Encoder
{
    protected boolean _encoder_ready = false;
    protected int _encoder_size      = -1;

    abstract public void build(final int encoder_size, final int input_size);

    public void build(final int encoder_size,
                      final int input_size,
                      final double[][] training_inputs,
                      final String save_filename)
    {
        // No training necessary per default.
        System.out.println("Encoder: No training required !");
        // Call build function.
        build(encoder_size, input_size);
    }

    public void build(final int encoder_size,
                      final int input_size,
                      final String load_filename)
    {
        // No weights to load per default.
        System.out.println("Encoder: No weights to load !");
        // Call build function.
        build(encoder_size, input_size);
    }

    abstract public double[] encoding(final int[] input);

    abstract public int flatten(final int[] input);

    public int encoderSize() { return _encoder_size; }

    public boolean encoderReady() { return _encoder_ready; }
}
