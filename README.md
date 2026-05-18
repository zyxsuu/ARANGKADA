<h1 align="center">🏍️ ARANGKADA</h1>

<h3 align="center"><b>Keep track of every kilometer.</b></h3>

<p align="center">
  <b>A smart, all-in-one shift tracker, financial calculator, and maintenance logger</b> designed specifically for motorcycle delivery riders.<br/>
  Built as a major course project for <b>Application Development (AppDet) at UMak</b>, this Android application helps riders track their daily routes, compute net earnings, and maintain their vehicles efficiently without the hassle.
</p>

---

## Table of Contents

- [✨ Core Features](#-core-features)
- [🛠️ Technical Stack](#️-technical-stack)
- [🚀 Getting Started](#-getting-started)
- [👥 Contributors](#-contributors)

---

<a id="screenshots"></a>

<details>
<summary><h2>📸 Screenshots</h2></summary>

<p align="center">
  <i>(UI Screenshots coming soon once the dashboard and shift forms are complete!)</i>
</p>

</details>

---

## ✨ Core Features

| Feature | Description |
| :--- | :--- |
| **📍 Real-Time Tracking** | Utilizes Android Foreground Services and GPS to track the exact distance traveled (in kilometers) during a shift. |
| **💰 Financial Computations** | Automatically calculates Daily Net Profit (Gross Earnings - Gas Expense) and Fuel Efficiency (Km/L). |
| **🏍️ Virtual Odometer** | Keeps a running total of the motorcycle's overall mileage by adding completed shift distances to a lifetime total. |
| **📊 Daily Dashboard** | A clean, historical log of past shifts, net income, and fuel consumption trends over time. |
| **🐖 Sinking Fund** | Automatically slices a user-defined percentage from daily net income into a virtual savings goal. |
| **📄 CSV Exporter** | One-click export of shift and expense history for spreadsheet backup and tax preparation. |

### 🛠️ Automated Maintenance Alerts

Push notifications triggered by specific mileage thresholds:

- **1,500 Km:** 🛢️ Engine Oil Change
- **4,000 Km:** ⚙️ Spark Plug & Throttle Body Check
- **10,000 Km:** 🛑 Brake Pad & Belt/Chain Replacement

---

## 🛠️ Technical Stack

| Category | Technology / Library |
| :--- | :--- |
| **Language** | Java |
| **Environment** | Android Studio |
| **Local Storage** | Room Persistence Library / SQLiteOpenHelper |
| **Location Services** | FusedLocationProviderClient (Google Play Services) |
| **Background Processing** | WorkManager & Android Foreground Services |

---

## 🚀 Getting Started

To run this project locally on your machine:

1. Clone this repository:
   ```bash
   git clone [https://github.com/zyxsuu/ARANGKADA.git](https://github.com/zyxsuu/ARANGKADA.git)