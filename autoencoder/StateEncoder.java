package autoencoder;

import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationReLU;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.sgd.StochasticGradientDescent;
import org.encog.persist.EncogDirectoryPersistence;

import java.io.File;

public class StateEncoder
{
    private BasicNetwork _network = new BasicNetwork();

    private final int ENCODER_SIZE      = 2;
    private final int ENCODER_LAYER     = 1;
    private final double LEARNING_RATE  = 1e-6;
    private final int BATCH_SIZE        = 100;
    private final double TERMINAL_ERROR = 0.025;

    private boolean network_ready  = false;

    public void buildNetwork(final int input_size)
    {
        _network.addLayer(new BasicLayer(new ActivationReLU(), true, input_size));
        _network.addLayer(new BasicLayer(new ActivationLinear(), true, ENCODER_SIZE));
        _network.addLayer(new BasicLayer(new ActivationReLU(), true, input_size));
        _network.getStructure().finalizeStructure();
        _network.reset();
        // Set network status to ready (permit evaluation).
        network_ready = true;
    }

    public void adapt(final double[][] training_inputs)
    {
        // Build training data set.
        final MLDataSet training_data = new BasicMLDataSet(training_inputs, training_inputs);
        // Train network (inputs = label as encoder).
        int epoch = 1; double last_error;
        StochasticGradientDescent train = new StochasticGradientDescent(_network, training_data);
        train.setLearningRate(LEARNING_RATE);
        train.setBatchSize(BATCH_SIZE);
        do {
            last_error = train.getError();
            train.iteration();
            System.out.printf("Epoch #%d, Error = %f, Grad = %f\n",
                              epoch, train.getError(), Math.abs(train.getError() - last_error));
            epoch++;
        } while (Math.abs(train.getError()) > TERMINAL_ERROR);
        train.finishTraining();
    }

    public double[] encoding(final double[] input)
    {
        final MLData input_data = new BasicMLData(input);
        _network.compute(input_data);
        double[] encoded_layer = new double[ENCODER_SIZE];
        for (int k = 0; k < ENCODER_SIZE; ++k)
        {
            encoded_layer[k] = _network.getLayerOutput(ENCODER_LAYER, k);
        }
        return encoded_layer;
    }

    public void store(final String filename)
    {
        EncogDirectoryPersistence.saveObject(new File(filename), _network);
    }

    public void load(final String filename)
    {
        _network = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File(filename));
        // Set network status to ready (permit evaluation).
        network_ready = true;
    }

    public int getEncoderSize() { return ENCODER_SIZE; }

    public boolean encoderReady() { return network_ready; }

}
