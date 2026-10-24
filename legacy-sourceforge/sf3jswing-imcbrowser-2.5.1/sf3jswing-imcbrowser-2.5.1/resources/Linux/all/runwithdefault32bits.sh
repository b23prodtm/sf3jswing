#!/bin/sh
LD_PRELOAD=/usr/lib/libv4l/v4l1compat.so LD_LIBRARY_PATH=/home/$USER:"$LD_LIBRARY_PATH" /usr/java/default/bin/java -cp /home/$USER/.ImcBrowser/$USER:sf3jswing-imcbrowser-Linux-i386_signed.jar net.sf.jiga.xtended.ui.JFCFrame -antframe jxa.IMCBrowser -width 800 -license
