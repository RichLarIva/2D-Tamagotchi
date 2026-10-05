package se.zodiakengine.richard.GameInnards.Scenes;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {
    private final String vertexShaderSource =
            "#version 330 core\n" +
                    "layout (location = 0) in vec3 aPos;\n" +
                    "layout (location = 1) in vec4 aColor;\n" +
                    "\n" +
                    "out vec4 fColor;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    fColor = aColor;\n" +
                    "    gl_Position = vec4(aPos, 1.0);\n" +
                    "}";

    private final String fragmentShaderSource =
            "#version 330 core\n" +
                    "\n" +
                    "in vec4 fColor;\n" +
                    "\n" +
                    "out vec4 color;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    color = fColor;\n" +
                    "}";

    private final float[] vertexArray = {
            // position                 // color
            0.5f, -0.5f, 0.0f,   /**/   1.0f, 0.0f, 0.0f, 1.0f, // Bottom Right 0
            -0.5f, 0.01f, 0.0f,   /**/   0.0f, 1.0f, 0.0f, 1.0f, // top left 1
            0.15f, 0.5f, 0.0f,    /**/   0.0f, 0.0f, 1.0f, 1.0f,  // Top right 2
            -0.5f, -0f, 0.0f,  /**/   1.0f, 1.0f, 0.0f, 1.0f, // bottom left 3
    };

    //IMPORTANT: MUST BE IN COUNTER-CLOCKWISE ORDER
    private final int[] elementArray = {
            2, 1, 0, // Top Right triangle
            0, 1, 3, // Bottom left triangle
    };

    private int vertexID, fragmentID, shaderProgram;

    private int vaoID, vboID, eboID;

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        // Compile and link shaders

        //First load and compile the vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(vertexID, vertexShaderSource);
        glCompileShader(vertexID);
        glEnable(GL_DEPTH_TEST);
        // Check for errors in compilation
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE)
        {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            IO.println("ERROR WOOPSIES: 'defaultShader.glsl'\n\r\tVertex shader compilation failed.");
            IO.println(glGetShaderInfoLog(vertexID, len));
            throw new RuntimeException("Vertex Shader compilation failed:\n\r" + glGetShaderInfoLog(vertexID, len));
        }

        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(fragmentID, fragmentShaderSource);
        glCompileShader(fragmentID);

        // Check for errors in compilation
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE)
        {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            IO.println("ERROR WOOPSIES: 'defaultShader.glsl'\n\r\tFragment shader compilation failed.");
            IO.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        // Link shaders and check for errors
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexID);
        glAttachShader(shaderProgram, fragmentID);
        glLinkProgram(shaderProgram);

        //Check for linking errors
        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE)
        {
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            IO.println("ERROR WOOPSIES: 'defaultShader.glsl'\n\r\tLinking of shaders failed.");
            IO.println(glGetProgramInfoLog(shaderProgram));
            assert false : "";
        }

        // GENERATE VAO, VBO, and EBO buffer objects, and send to GPU
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // Create VBO uploda the vertex buffer
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Create the indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // Add the vertex attribute pointers
        int positionsSize = 3;
        int colorSize = 4;
        int floatSizeBytes = 4;
        int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * floatSizeBytes);
        glEnableVertexAttribArray(1);
    }

    @Override
    public void update(float deltaTime) {
        // bind shader program
        glUseProgram(shaderProgram);
        //Bind the VAO that we are using
        glBindVertexArray(vaoID);

        //enable the vertex attribute pointers
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        //unbind everything
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);

        glUseProgram(0);
    }
}
