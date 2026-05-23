import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {

  private readonly STORAGE_KEY = 'superconvert-theme';
  private oscuro: boolean = false;

  constructor() {
    const guardado = localStorage.getItem(this.STORAGE_KEY);
    if (guardado !== null) {
      this.oscuro = guardado === 'dark';
    } else {
      this.oscuro = window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    this.aplicarTema();
  }

  isDark(): boolean {
    return this.oscuro;
  }

  toggle(): void {
    this.oscuro = !this.oscuro;
    localStorage.setItem(this.STORAGE_KEY, this.oscuro ? 'dark' : 'light');
    this.aplicarTema();
  }

  private aplicarTema(): void {
    if (this.oscuro) {
      document.body.classList.add('dark-mode');
    } else {
      document.body.classList.remove('dark-mode');
    }
  }
}
