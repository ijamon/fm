#ifndef DIRLIB_GLOBAL_H
#define DIRLIB_GLOBAL_H

#include <QtCore/qglobal.h>

#if defined(DIRLIB_LIBRARY)
#  define DIRLIBSHARED_EXPORT Q_DECL_EXPORT
#else
#  define DIRLIBSHARED_EXPORT Q_DECL_IMPORT
#endif

#endif // DIRLIB_GLOBAL_H
