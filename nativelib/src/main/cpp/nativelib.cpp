#include <jni.h>
#include <string>
#include <sstream>
#include <iostream>
#include "uci.h"
#include "thread.h"
#include "position.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_program_nativelib_NativeLib_getBestMove(
        JNIEnv* env,
        jobject /* this */,
        jstring fen,
        jint searchTimeMillis) {

    UCI::init(Options);
    Position::init();
    Threads.set(1);

    const char* fenChars = env->GetStringUTFChars(fen, nullptr);
    std::string fenStr(fenChars);
    env->ReleaseStringUTFChars(fen, fenChars);

    std::stringstream input_stream;
    input_stream << "position fen " << fenStr << std::endl;
    input_stream << "go movetime " << searchTimeMillis << std::endl;

    std::stringstream output_stream;
    auto old_cout_buf = std::cout.rdbuf();
    std::cout.rdbuf(output_stream.rdbuf());

    UCI::loop(input_stream);

    std::cout.rdbuf(old_cout_buf);

    std::string result_line;
    std::string bestMove = "error";
    while (std::getline(output_stream, result_line)) {
        if (result_line.rfind("bestmove", 0) == 0) {
            std::stringstream ss(result_line);
            std::string temp;
            ss >> temp;
            ss >> bestMove;
            break;
        }
    }

    return env->NewStringUTF(bestMove.c_str());
}