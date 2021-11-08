import tkinter as tk
#tạo cửa sổ
window = tk.Tk()
#Nhãn
lable = tk.Label(text = "hello tkinter",foreground = "red",background = "black",width = 20,height = 10)
#button
# button = tk.Button(text = "Click???",width = 10,height = 10,bg = "blue",fg = "yellow")
#entry
entry = tk.Entry()
# lable.pack()
# entry.pack()
#lấy nội dung
name = entry.get()
#textwidget

text_box = tk.Text()
# text_box.pack()
#lấy nội dung "1.0" chỉ số "hàng.vijtri"
text = text_box.get("1.0")
#frame
# frame = tk.Frame()
# lb = tk.Label(master=frame,text = "đây la frame")
# lb.pack()
# frame.pack()
#frame efect
flat = tk.GROOVE
frame = tk.Frame(master=window,relief = flat,borderwidth = 5)
lb = tk.Label(master=frame,text = "flat")
button = tk.Button(text = "flat")
frame.pack(side=tk.LEFT)
lb.pack()
button.pack()
window.mainloop()