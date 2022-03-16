#-------------------------------------------------
#
# Project created by QtCreator 2016-10-30T14:31:28
#
#-------------------------------------------------

QT       -= gui

TARGET = DirLib
TEMPLATE = lib

DEFINES += DIRLIB_LIBRARY

SOURCES += dirlib.cpp

HEADERS += dirlib.h \
    dirlib_global.h
	
win32:INCLUDEPATH += "C:/Program Files/Java/jdk1.7.0_79/include/"
win32:INCLUDEPATH += "C:/Program Files/Java/jdk1.7.0_79/include/win32/"
	
unix {
    target.path = /usr/lib
    INSTALLS += target
}
