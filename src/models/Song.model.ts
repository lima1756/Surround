import mongoose, { Document } from 'mongoose';

export const SongSchema = new mongoose.Schema({
    id: String,
    name: String,
    artist: String,
    imgFile: String,
    songFile: String,
    duration: Number
})

export interface ISong extends Document {
    id: string,
    name: string,
    artist: string,
    imgFile: string,
    songFile: string,
    duration: number
}

const model = mongoose.model<ISong>('Song', SongSchema);;

export class SongHelper{
    public static async findById(id: string):Promise<ISong|null>{
        return await model.findOne({"id": id});
    }

}

export default model;