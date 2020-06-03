import { OK, BAD_REQUEST, NOT_FOUND } from 'http-status-codes';
import { Controller, Get, Post } from '@overnightjs/core';
import { Request, Response } from 'express';
import fileUpload from 'express-fileupload';
import path from 'path'
import Song, {ISong, SongHelper} from '../../models/Song.model';
import AWS from 'aws-sdk';
import { PutObjectRequest, GetObjectRequest } from 'aws-sdk/clients/s3';
import {fs} from 'memfs';
import { Logger } from '@overnightjs/logger';
import getMP3Duration from 'get-mp3-duration';
require('dotenv').config()
AWS.config.update({
    region: 'us-west-1',
    accessKeyId: process.env.AWSAccessKeyId,
    secretAccessKey: process.env.AWSSecretKey
});
const S3 = new AWS.S3();


@Controller('api/music')
class MusicController {

    public static readonly SUCCESS_MSG = 'hello ';

    @Get('all')
    private async getAllSongs(req: Request, res: Response){
        Song.find({}).select('-_id -imgFile -songFile -__v').then(songs=>{
            res.send(songs);
        })
    }

    @Get('data/:id')
    private async getData(req: Request, res: Response){
        try {
            const song = await SongHelper.findById(req.params.id);
            if(!song){
                res.send({})
                return;
            }
            res.send({
                id: song!.id,
                name: song!.name,
                artist: song!.artist,
                duration: song!.duration
            })            
        } catch (err) {
            console.log(err);
            res.sendStatus(BAD_REQUEST);
        }
    }

    @Get('song/:id')
    private async getSong(req: Request, res: Response){
        try {            
            const song = await SongHelper.findById(req.params.id);
            const exists = fs.existsSync(path.join(__dirname, '../../../public/songs', song!.songFile));
            Logger.Info(exists);
            if(exists){
                res.sendFile(path.join(__dirname, '../../../public/songs', song!.songFile));            
                return;
            }
            await downloadFile(process.env.AWS_BUCKET || "", "public/songs/"+song!.songFile, res);
        } catch (err) {
            Logger.Err(err);
            res.sendStatus(NOT_FOUND);
        }
    }

    @Get('image/:id')
    private async getImage(req: Request, res: Response){
        try {
            const song = await SongHelper.findById(req.params.id);
            const exists = fs.existsSync(path.join(__dirname, '../../../public/images', song!.imgFile));
            if(exists){
                res.sendFile(path.join(__dirname, '../../../public/images', song!.imgFile));    
                return;
            }
            downloadFile(process.env.AWS_BUCKET || "", "public/images/"+song!.imgFile, res);
        } catch (err) {
            Logger.Err(err);
            res.sendStatus(BAD_REQUEST);
        }
    }

    @Post('')
    private async addSong(req: Request, res: Response){
        if(!req.files && req.files!.song && req.files!.img){
            res.sendStatus(BAD_REQUEST);
        }
        const songFile = req.files!.song as fileUpload.UploadedFile;
        const imgFile = req.files!.img as fileUpload.UploadedFile;
        let songId = Date.now()+"";
        Logger.Info("duration: " + getMP3Duration(songFile.data));
        const song: ISong = new Song({
            id: songId,
            name: req.body.name,
            artist: req.body.artist,
            imgFile: songId+"_"+imgFile.name,
            songFile: songId+"_"+songFile.name,
            duration: getMP3Duration(songFile.data)
        })
        await imgFile.mv(path.join(__dirname, '../../../public/images', song.imgFile));
        await songFile.mv(path.join(__dirname, '../../../public/songs', song.songFile));
        uploadFile(process.env.AWS_BUCKET  || "", "public/images/"+song.imgFile, imgFile.data);    
        uploadFile(process.env.AWS_BUCKET  || "", "public/songs/"+song.songFile, songFile.data);
        song.save();
        res.send({
            "id": songId
        });
    }
}

async function downloadFile(Bucket: string, Key: string, res: Response) {
    const request : GetObjectRequest = {
        Bucket: Bucket,
        Key: Key
    };
    const stream = S3.getObject(request).createReadStream();
    stream.on('error', (err)=>{Logger.Err(err); res.sendStatus(NOT_FOUND)})
    stream.pipe(res);
}

function uploadFile(bucket: string, key: string, data: Buffer){
    const songParams : PutObjectRequest = {
        Bucket: bucket,
        Body : data,
        Key : key
    };
    S3.upload(songParams, function (err, data) {
        if (err) {
            console.log("Error", err);
        }
        if (data) {
            console.log("Uploaded in:", data.Location);
        }
    });
}

export default MusicController;