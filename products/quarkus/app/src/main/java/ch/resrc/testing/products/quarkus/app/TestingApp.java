package ch.resrc.testing.products.quarkus.app;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class TestingApp {

    public static void main(String[] args) {
        Quarkus.run(args);
    }

}
