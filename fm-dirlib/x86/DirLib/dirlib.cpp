#include "dirlib.h"
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT jstring JNICALL Java_io_cnsoft_services_wrapper_DirIndexerWrapper_getFiles(JNIEnv *env, jobject obj, jstring path)
{
    const char *nativeString = (*env).GetStringUTFChars(path, false);

    // use your string
    QString spath = QString(nativeString);
    (*env).ReleaseStringUTFChars(path, nativeString);

    QJsonObject jInfo;
    jInfo.insert("result","0");

    QJsonArray jData;
//    spath =
    QDirIterator it(spath);
    while (it.hasNext()) {
        it.next();

        QJsonObject lineInfo;
        lineInfo.insert("path", it.filePath());
        lineInfo.insert("name",it.fileName());
        lineInfo.insert("lastModified", it.fileInfo().lastModified().toMSecsSinceEpoch());

        lineInfo.insert("isDirectory",it.fileInfo().isDir());

        if (it.fileInfo().isFile())
            lineInfo.insert("lengthBytes",QString::number(it.fileInfo().size()));

        jData.append(lineInfo);
    }

    jInfo.insert("data",jData);


    jstring ret = (*env).NewStringUTF(QJsonDocument(jInfo).toJson());
    return ret;
}

#ifdef __cplusplus
}
#endif


DirLib::DirLib()
{
}
