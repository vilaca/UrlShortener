package pt.go2.Mocks;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class ServletOutputStreamMock extends ServletOutputStream {

    public ServletOutputStreamMock() {
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener arg0) {
    }

    @Override
    public void write(int b) throws IOException {
    }

}
