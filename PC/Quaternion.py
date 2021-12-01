from multiprocessing.context import Process
import tkinter as tk
import tkinter.filedialog as fd
from tkinter import messagebox
from typing import List, Tuple
import qrcode
import os
import socket
from PIL import ImageTk
import multiprocessing
import shutil
from subprocess import call
from pyftpdlib.authorizers import DummyAuthorizer
from pyftpdlib.handlers import FTPHandler
from pyftpdlib.servers import FTPServer
from qrcode.image.pil import PilImage


SEND = True
RECEIVE = False
IP = '0.0.0.0'
PORT = 21
USER = 'FT'
PASSWD='PASSWD'
DEST_SEND='.\Send'
DEST_RECEIVE = '.\Receive'



class FileServer():
    def serve(self, ipaddr, port=21, username='ftp', passwd='ftp', dest='.', perm='elradfmwMT'):
        authorizer = DummyAuthorizer()
        authorizer.add_user(username, passwd, dest, perm)
        handler = FTPHandler
        handler.authorizer = authorizer
        server = FTPServer((ipaddr, port), handler)
        server.serve_forever()


def _copyfileobj_patched(fsrc, fdst, length=16*1024*1024):
    """Patches shutil method to hugely improve copy speed"""
    while 1:
        buf = fsrc.read(length)
        if not buf:
            break
        fdst.write(buf)
shutil.copyfileobj = _copyfileobj_patched


def main():
    if not os.path.exists('.\Send'):
        os.makedirs('.\Send')

    if not os.path.exists('.\Receive'):
        os.makedirs('.\Receive')

    window = tk.Tk()
    window.title("Quaternion")
    window.resizable(False,False)
    window.geometry('500x150')
    window.eval('tk::PlaceWindow . center')
    
    btnSend = tk.Button()
    btnSend['text'] = 'Send'
    btnSend['command'] = lambda : sendBtnAction(window, btnSend)

    btnReceive = tk.Button()
    btnReceive['text'] = 'Receive'
    btnReceive['command'] = lambda : receiveBtnAction(window, btnReceive)

    btnExit = tk.Button()
    btnExit['text'] = "Exit"
    btnExit['command'] = window.destroy


    btnSend.pack()
    btnReceive.pack()
    btnExit.pack()
    window.mainloop()


def chooseFile(window : tk.Toplevel):
    files = fd.askopenfilenames(parent=window, title='Choose File(s)')
    return files



def sendBtnAction(window : tk.Toplevel, btnSend : tk.Button):
    #files = chooseFile(window)
    #copyFiles(window, files, btnSend)
    qr = getQRCode(SEND)
    openQRWindow(window, qr, btnSend, SEND)
    


def receiveBtnAction(window : tk.Toplevel, btnReceive : tk.Button):
   qr = getQRCode(RECEIVE)
   openQRWindow(window, qr, btnReceive, RECEIVE)




def copyFiles(window : tk.Tk, files : Tuple, btnSend : tk.Button):
    
    msg = tk.Label(window)
    msg['text'] = 'Processing... \n Please wait'
    msg.pack()
    btnSend['state'] = 'disabled'
    window.update_idletasks()
    for file in files:
        call(["xcopy", file.replace('/', '\\'), ".\FILES", "/K"])

    msg.destroy()



    
def getQRCode(state : bool):
    ip = socket.gethostbyname(socket.gethostname())
    port = PORT
    if state is SEND:
        qr = qrcode.make("QUAT#SND#" + ip + ':' + str(port))
        return qr
    elif state is RECEIVE:
        qr = qrcode.make("QUAT#RCV#" + ip + ':' + str(port))
        return qr




def serveFiles(state : bool):
    server = FileServer()
    if state is SEND:
        server.serve(IP, PORT, USER, PASSWD, DEST_SEND)
        print('snd')
    elif state is RECEIVE:
        server.serve(IP, PORT, USER, PASSWD, DEST_RECEIVE)
        print('rcv')
    
        
    


def openQRWindow(app : tk.Toplevel, qr : PilImage, btnReq : tk.Button, state : bool):
    app.withdraw()
    PROC : list[Process]
    PROC = []
    qrWindow = tk.Toplevel(app)
    if state is RECEIVE:
        qrWindow.title('Receiving File(s)')
    elif state is SEND:
        qrWindow.title('Sending File(s)')
    qrWindow.protocol("WM_DELETE_WINDOW", lambda : exit(qrWindow, btnProc, app))
    img = ImageTk.PhotoImage(qr)
    panel = tk.Label(qrWindow, image=img)
    panel.pack(side = "bottom", fill = "both", expand = "yes")
    btnExit = tk.Button(qrWindow)
    btnExit['text'] = 'Close'
    btnExit['command'] = lambda : exit(qrWindow, btnProc, app)
    btnProc = tk.Button(qrWindow)
    btnProc['text'] = 'Init'
    btnProc['command'] = lambda : toggleProc(btnProc, PROC, state)
    btnExit.pack()
    btnProc.pack()
    qrWindow.mainloop()



def exit(main : tk.Toplevel, btnProc : tk.Button, app : tk.Toplevel):
    if btnProc['text'] == 'Init':
        main.destroy()
        app.deiconify()
    else:
        messagebox.showerror("Error","You should stop transmission first!")




def toggleProc(btn : tk.Button, PROC : List, state : bool):
    
    if len(PROC) == 0:
        proc = multiprocessing.Process(target=serveFiles, args=(state,))
        proc.start()
        PROC.append(proc)
        btn['text'] = 'Stop'

    else:
        proc = PROC[0]
        proc.terminate()
        PROC.clear()
        btn['text'] = 'Init'


  

if __name__ == '__main__':
    main()