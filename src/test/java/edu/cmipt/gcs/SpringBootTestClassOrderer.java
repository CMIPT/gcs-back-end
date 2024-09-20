package edu.cmipt.gcs;

import edu.cmipt.gcs.controller.AuthenticationControllerTest;
import edu.cmipt.gcs.controller.RepositoryControllerTest;
import edu.cmipt.gcs.controller.UserControllerTest;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

public class SpringBootTestClassOrderer implements ClassOrderer {

    private static final Class<?>[] classOrder =
            new Class[] {
                AuthenticationControllerTest.class,
                RepositoryControllerTest.class,
                UserControllerTest.class
            };

    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext
                .getClassDescriptors()
                .sort(Comparator.comparingInt(SpringBootTestClassOrderer::getOrder));
    }

    private static int getOrder(ClassDescriptor classDescriptor) {
        for (int i = 0; i < classOrder.length; i++) {
            if (classDescriptor.getTestClass().equals(classOrder[i])) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }
}
