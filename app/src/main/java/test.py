import os
import random
import threading
import tkinter as tk
from tkinter import filedialog, messagebox
from moviepy.editor import VideoFileClip, concatenate_videoclips
import moviepy.video.fx.all as vfx

class VideoGeneratorApp:
    def __init__(self, root):
        self.root = root
        self.root.title("🎬 Generator Teł Wideo")
        self.root.geometry("450x350")
        self.root.resizable(False, False)

        # --- Elementy interfejsu ---
        
        # 1. Wybór folderu
        tk.Label(root, text="1. Wybierz folder z filmami (tłami):", font=("Arial", 10, "bold")).pack(pady=(15, 5))
        self.folder_path = tk.StringVar()
        folder_frame = tk.Frame(root)
        folder_frame.pack()
        tk.Entry(folder_frame, textvariable=self.folder_path, width=40, state='readonly').pack(side=tk.LEFT, padx=5)
        tk.Button(folder_frame, text="Przeglądaj", command=self.browse_folder).pack(side=tk.LEFT)

        # 2. Ilość wideo
        tk.Label(root, text="2. Ile filmów wylosować i połączyć?", font=("Arial", 10, "bold")).pack(pady=(15, 5))
        self.num_clips = tk.StringVar(value="5")
        tk.Entry(root, textvariable=self.num_clips, width=10, justify="center").pack()

        # 3. Przyspieszenie
        tk.Label(root, text="3. Przyspieszenie (1.0 = normalnie, 2.0 = 2x szybciej):", font=("Arial", 10, "bold")).pack(pady=(15, 5))
        self.speed_factor = tk.StringVar(value="1.0")
        tk.Entry(root, textvariable=self.speed_factor, width=10, justify="center").pack()

        # Przycisk startu
        self.start_btn = tk.Button(root, text="🚀 Generuj Wideo", font=("Arial", 12, "bold"), bg="#4CAF50", fg="white", command=self.start_generation_thread)
        self.start_btn.pack(pady=20)

        # Status
        self.status_label = tk.Label(root, text="Gotowy do pracy.", fg="gray")
        self.status_label.pack()

    # --- Logika aplikacji ---

    def browse_folder(self):
        folder_selected = filedialog.askdirectory(title="Wybierz folder z wideo")
        if folder_selected:
            self.folder_path.set(folder_selected)

    def start_generation_thread(self):
        # Uruchamiamy generowanie w osobnym wątku, aby okno programu nie "zamarzło"
        threading.Thread(target=self.generate_video, daemon=True).start()

    def generate_video(self):
        folder = self.folder_path.get()
        
        # Walidacja danych wejściowych
        if not folder:
            messagebox.showwarning("Błąd", "Najpierw wybierz folder z plikami wideo!")
            return

        try:
            num = int(self.num_clips.get())
            speed = float(self.speed_factor.get())
            if num <= 0 or speed <= 0:
                raise ValueError
        except ValueError:
            messagebox.showwarning("Błąd", "Wprowadź poprawne wartości liczbowe dla ilości wideo i przyspieszenia (np. 5 i 1.5).")
            return

        self.start_btn.config(state=tk.DISABLED, text="⏳ Trwa renderowanie...")
        self.status_label.config(text="Szukanie plików wideo...", fg="blue")

        try:
            supported_formats = ('.mp4', '.mov', '.avi', '.mkv')
            all_files = [f for f in os.listdir(folder) if f.lower().endswith(supported_formats)]

            if not all_files:
                messagebox.showerror("Błąd", "W wybranym folderze nie ma plików wideo!")
                self.reset_ui()
                return

            if num > len(all_files):
                selected_files = random.choices(all_files, k=num)
            else:
                selected_files = random.sample(all_files, num)

            self.status_label.config(text=f"Łączenie {num} klipów. To potrwa kilka chwil...")
            
            clips = []
            for file in selected_files:
                full_path = os.path.join(folder, file)
                clips.append(VideoFileClip(full_path))

            final_clip = concatenate_videoclips(clips, method="compose")

            if speed != 1.0:
                self.status_label.config(text="Nakładanie efektu przyspieszenia...")
                final_clip = final_clip.fx(vfx.speedx, speed)

            final_clip = final_clip.without_audio()

            output_path = os.path.join(folder, "gotowe_tlo_bez_dzwieku.mp4")
            self.status_label.config(text="Renderowanie i zapisywanie pliku (może chwilę zająć)...")
            
            # Parametry logger=None wyciszają spam w konsoli podczas renderowania
            final_clip.write_videofile(output_path, codec="libx264", audio=False, fps=30, threads=4, logger=None)

            # Sprzątanie pamięci
            for clip in clips:
                clip.close()
            final_clip.close()

            self.status_label.config(text="✅ Zakończono sukcesem!", fg="green")
            messagebox.showinfo("Sukces!", f"Wideo zostało wygenerowane pomyślnie!\n\nZnajdziesz je tutaj:\n{output_path}")

        except Exception as e:
            messagebox.showerror("Błąd krytyczny", f"Wystąpił problem podczas renderowania:\n{str(e)}")
            self.status_label.config(text="Wystąpił błąd.", fg="red")
        
        finally:
            self.reset_ui()

    def reset_ui(self):
        self.start_btn.config(state=tk.NORMAL, text="🚀 Generuj Wideo")

if __name__ == "__main__":
    root = tk.Tk()
    app = VideoGeneratorApp(root)
    root.mainloop()