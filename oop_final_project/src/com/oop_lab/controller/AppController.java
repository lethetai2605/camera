package com.oop_lab.controller;

import com.oop_lab.app.CameraApp;
import com.oop_lab.model.Camera;
import com.oop_lab.model.DoVat;
import com.oop_lab.model.Room;
import com.oop_lab.model.khong_gian.ToaDo;
import com.oop_lab.service.CameraService;
import com.oop_lab.service.DoVatService;
import com.oop_lab.service.RoomService;
import com.oop_lab.view.AppView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class AppController {

    private CameraApp app;
    private AppView view;
    private DoVatService doVatService;
    private CameraService cameraService;
    private RoomService roomService;

    public AppController(
            CameraApp app,
            AppView view,
            CameraService cameraService,
            DoVatService doVatService,
            RoomService roomService
    ) {
        this.app = app;
        this.view = view;
        this.cameraService = cameraService;
        this.doVatService = doVatService;
        this.roomService = roomService;
    }

    public void runApp() {
        this.app.setRunning(true);

        while (this.app.isRunning()) {
            int userOption = this.view.menu(this.app.getName(),
                        "read file input", "show result", "exit");
            switch (userOption) {
                case 1:
                    this.readDataFromInputFile();
                    break;
                case 2:
                    this.showResult();
                    break;
                case 3:
                    this.view.notice("message", "goodbye");
                    this.app.setRunning(false);
                    break;
                default:
                    this.view.notice("warning", "we do not support this function. enter again");
            }
        }
    }

    public void readDataFromInputFile() {
        this.view.notice(null, "enter the path of file input: ");
        String filePath = this.view.getKeyBoard().nextLine();
        try {
            File inputFile = new File(filePath);
            Scanner sc = new Scanner(inputFile);

            // CREATE ROOM
            String currentLine = sc.nextLine();
            String[] datas = currentLine.replace("(", "")
                    .replace(")", "")
                    .replace(",", "").split(" ");

            List<ToaDo> cacDinhCuaPhong = new ArrayList<ToaDo>();
            for (int i = 0; i < 24; i += 3) {
                cacDinhCuaPhong.add(new ToaDo(
                        Double.parseDouble(datas[i]),
                        Double.parseDouble(datas[i + 1]),
                        Double.parseDouble(datas[i + 2])));
            }
            this.app.setRoom(this.createRoom(cacDinhCuaPhong));
            if (this.app.getRoom() == null) return;

            // CREATE DO VAT AND ADD INTO ROOM
            currentLine = sc.nextLine();
            int soLuongDoVat = Integer.parseInt(currentLine);
            for (int i = 0; i < soLuongDoVat; i++) {
                currentLine = sc.nextLine();
                datas = currentLine.replace("(", "")
                        .replace(")", "")
                        .replace(",", "").split(" ");

                List<ToaDo> cacDinhCuaDoVat = new ArrayList<ToaDo>();
                for (int j = 0; j < 24; j += 3) {
                    cacDinhCuaPhong.add(new ToaDo(
                            Double.parseDouble(datas[j]),
                            Double.parseDouble(datas[j + 1]),
                            Double.parseDouble(datas[j + 2])));
                }
                DoVat doVat = this.createDoVat(cacDinhCuaDoVat);
                if (doVat != null)
                    this.themDoVatVaoPhong(doVat);
            }

            // CREATE CAMERA AND ADD INTO ROOM
            currentLine = sc.nextLine();
            int soLuongCamera = Integer.parseInt(currentLine);
            for (int i = 0; i < soLuongCamera; i++) {
                currentLine = sc.nextLine();
                datas = currentLine.replace("(", "").replace(")", "")
                        .replace(",", "").split(" ");
                ToaDo toaDo = new ToaDo(
                        Double.parseDouble(datas[0]),
                        Double.parseDouble(datas[1]),
                        Double.parseDouble(datas[2])
                );
                double gocRong = Double.parseDouble(datas[3]);
                double gocCao = Double.parseDouble(datas[4]);
                Camera camera = this.createCamera(toaDo, gocCao, gocRong);
                if (camera != null)
                    this.themCameraVaoPhong(camera);
            }

            sc.close();
        } catch (FileNotFoundException e) {
            this.view.notice("error", e.getMessage());
        }

    }

    public void showResult() {
        this.view.notice(null, "steps: ");
        String[] steps = this.view.getKeyBoard().nextLine().split(" ");
        int x = 10; // so diem duyet tren truc Ox
        int y = 10; // so diem duyet tren truc Oy
        int z = 10; // so diem duyet tren truc Oz
                    // --> tong so diem duyet : x * y * z (default = 1000)
        if (steps.length == 3) {
            x = Integer.parseInt(steps[0]);
            y = Integer.parseInt(steps[1]);
            z = Integer.parseInt(steps[2]);
        }

        Map<String ,String> results = new HashMap<String, String>();

        double theTichCanPhong = this.roomService.theTichPhong(this.app.getRoom());
        results.put("the tich phong", theTichCanPhong + "");

        double theTichVungNhinThay = this.roomService
                        .theTichVungNhinThay(this.app.getRoom(), x, y, z);
        results.put("the tich nhin thay", theTichVungNhinThay + "");

        results.put(
                "ty le vung nhin duoc so voi phong",
                (theTichVungNhinThay/theTichCanPhong) + " %"
        );

        this.view.showResult(results);
    }

    public Room createRoom(double chieuCao, double chieuRong, double chieuDai) {
        Room room = this.roomService.createRoom(chieuCao, chieuRong, chieuDai);

        if (room != null)
            this.view.notice("message", "successfully created room");
        else
            this.view.notice("error", "creating room is failed");

        return room;
    }

    public Room createRoom(List<ToaDo> cacDinh) {
        Room room = this.roomService.createRoom(cacDinh);

        if (room != null)
            this.view.notice("message", "successfully created room");
        else
            this.view.notice("error", "creating room is failed");

        return room;
    }

    public DoVat createDoVat(List<ToaDo> cacDinh) {
        DoVat doVat = this.doVatService.createDoVat(cacDinh);

        if (doVat != null)
            this.view.notice("message", "successfully created DoVat");
        else
            this.view.notice("warning", "creating DoVat failed");

        return doVat;
    }

    public Camera createCamera(ToaDo toaDo, double gocCao, double gocRong) {
        Camera camera = this.cameraService.createCamera(toaDo, gocCao, gocRong);

        if (camera != null)
            this.view.notice("message", "successfully created " + camera);
        else
            this.view.notice("warning", "creating " + camera + " failed");

        return camera;
    }

    public void themDoVatVaoPhong(DoVat doVat) {
        if (this.roomService.themDoVat(this.app.getRoom(), doVat)) {
            this.view.notice("message", "successfully added doVat to the room");
        } else {
            this.view.notice("warning", "adding doVat to room failed");
        }
    }

    public void themCameraVaoPhong(Camera camera) {
        if (this.roomService.themCamera(this.app.getRoom(), camera)) {
            this.view.notice("message", "successfully added " + camera + " to the room");
        } else {
            this.view.notice("warning", "adding " + camera + " to room failed");
        }
    }
}














